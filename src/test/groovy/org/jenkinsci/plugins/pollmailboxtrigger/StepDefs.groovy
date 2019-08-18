package org.jenkinsci.plugins.pollmailboxtrigger

import cucumber.api.DataTable
import cucumber.api.java.After
import cucumber.api.java.Before
import cucumber.api.java.en.Given
import cucumber.api.java.en.Then
import cucumber.api.java.en.When
import hudson.model.Action
import hudson.model.Cause
import hudson.model.ParametersAction
import hudson.model.Project
import hudson.util.FormValidation
import hudson.util.Secret
import hudson.util.StreamTaskListener
import org.jenkinsci.lib.xtrigger.XTriggerLog
import org.jenkinsci.plugins.pollmailboxtrigger.bdd.ConfigurationRow
import org.jenkinsci.plugins.pollmailboxtrigger.bdd.EmailRow
import org.jenkinsci.plugins.pollmailboxtrigger.environment.SetEnvironmentVariablesAction
import org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.CustomProperties
import org.jenkinsci.plugins.scripttrigger.LabelRestrictionClass
import org.jvnet.mock_javamail.Mailbox
import org.mockito.ArgumentCaptor

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.hamcrest.core.Is.is
import static org.hamcrest.core.StringContains.containsString
import static org.jenkinsci.plugins.pollmailboxtrigger.SafeJenkins.isNull
import static org.jenkinsci.plugins.pollmailboxtrigger.SafeJenkins.useNativeInstance
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.MessageBuilder.buildMessage
import static org.junit.Assert.fail
import static org.mockito.Mockito.*

public class StepDefs {

    Project job

    Mailbox inmemoryMailbox

    PollMailboxTrigger plugin

    FormValidation validation

    PollMailboxTrigger.PollMailboxTriggerDescriptor descriptor

    ConfigurationRow config

    CustomProperties effectiveConfig

    XTriggerLog log

    ByteArrayOutputStream logStream

    ArgumentCaptor<Integer> quietPeriodCaptor
    ArgumentCaptor<Cause> causeCaptor
    ArgumentCaptor<Action[]> actionsCaptor

    @Before
    void setup() {
        useNativeInstance(false)
        descriptor = new PollMailboxTrigger.PollMailboxTriggerDescriptor()
        logStream = new ByteArrayOutputStream()
        log = new XTriggerLog(new StreamTaskListener(logStream))
        job = mock(Project)
        // captors
        quietPeriodCaptor = ArgumentCaptor.forClass(Integer);
        causeCaptor = ArgumentCaptor.forClass(Cause);
        actionsCaptor = ArgumentCaptor.forClass(([] as Action[]).getClass());
    }

    @After
    void teardown() {
        inmemoryMailbox?.clear()
        if (logStream){
            logStream.close()
        }
    }

    @Given('the plugin is initialised')
    public void setup_plugin(){
        plugin = spy(new PollMailboxTrigger("*/5 * * * *", new LabelRestrictionClass("master"), false,
                '', '', new Secret(''), '', PollMailboxTrigger.AttachmentOptions.IGNORE.name()))

        // mocking plugin methods...
        doReturn(descriptor).when(plugin).getDescriptor()
        doReturn(job).when(plugin).getJob()
        doReturn([] as List<Action>).when(plugin).getScheduledXTriggerActions(log)
    }

    @Given('a mailbox with domain (.+) and username (.+)')
    public void setup_mailbox(String domain, String username) {
        inmemoryMailbox = Mailbox.get("$username@$domain")
    }

    @Given('the emails')
    public void and_emails(List<EmailRow> emails) {
        emails.each {
            inmemoryMailbox.add(buildMessage(it.subject, it.sentXMinutesAgo, it.isSeenFlag, it.from, it.body, it.attachments))
        }
    }

    @Given('the following Jenkins variables')
    public void setup_envvars(Map<String, String> envVars){
        SafeJenkins.setLocalNodeProperties(envVars);
    }

    @When('I set the configuration to')
    public void set_config(List<ConfigurationRow> rows) throws Throwable {
        if (isNull(config)){
            config = new ConfigurationRow();
        }
        if (!rows.isEmpty()){
            def newConf = rows.get(0)
            config.host = newConf.host
            config.username = newConf.username
            config.password = newConf.password
            config.appendScript(newConf.script)
            config.attachments = newConf.attachments
        }
        configUpdated(config)
    }

    private void configUpdated(ConfigurationRow config){
        plugin.setHost(config.host)
        plugin.setUsername(config.username)
        plugin.setPassword(config.buildPasswordSecret())
        plugin.setScript(config.script)
        plugin.setAttachments(config.attachments)
        effectiveConfig = PollMailboxTrigger.initialiseDefaults(config?.host, config?.username, config?.buildPasswordSecret(), config?.script, config?.attachments)
    }

    @When('the script')
    public void set_config(String script) throws Throwable {
        if (isNull(config)){
            config = new ConfigurationRow();
        }
        config.appendScript(script)
        configUpdated(config)
    }

    @When('I test the connection')
    public void i_test_the_connection() throws Throwable {
        validation = plugin.getDescriptor().doTestConnection(config.host, config.username, config?.buildPasswordSecret(), config.script, PollMailboxTrigger.AttachmentOptions.IGNORE.name())
    }

    @When('the Plugin\'s polling is triggered')
    public void trigger_polling() throws Throwable {
        // execute the polling...
        plugin.checkIfModified(null, log)
    }

    @Then('the effective configuration should be')
    public void the_config_should_be(DataTable expectedDataTable){
        List<List<String>> actualDataTable = effectiveConfig.getMap().collect { [it.key, it.value ?: ''] }.sort { a,b -> a[0] <=> b[0] }
        expectedDataTable.diff(actualDataTable)
    }

    @Then('^a Jenkins job is scheduled with quietPeriod (.+) and cause \'(.+)\'$')
    public void job_should_be_invoked(int expQuietPeriod, String expCauseDesc){
        // verify the job is invoked once (capture parameters)...
        verify(job, times(1)).scheduleBuild(quietPeriodCaptor.capture(), causeCaptor.capture(), actionsCaptor.capture())
        // verify job trigger params...
        assertThat(quietPeriodCaptor.getValue(), is(expQuietPeriod));
        assertThat(causeCaptor.getValue().getShortDescription(), is(expCauseDesc));
    }

    @Then('the Job parameters were - excluding (.*)')
    public void job_params_should_be(String excludeHeaders, DataTable expParameters){
        List<String> excludedHeaders = excludeHeaders ? excludeHeaders.split(',').collect() : []
        def parameters = getLastJobParameters()
        excludedHeaders.each {
            assertThat("parameter '$it' didn't exist, consider removing it from the acceptance test.", parameters.containsKey(it), is(true))
            parameters.remove(it)
        }
        expParameters.diff(parameters.collect { [it.key, it.value] })
    }

    @Then('there are (.+) saved attachments')
    public void saved_attachments(int expectedNumOfAttachments){
        List<File> files = new File(getLastJobParameters().get('pmt_attachmentsDirectory')).listFiles().collect()
        assertThat(files.size(), is(expectedNumOfAttachments))
        files.each {
            assertThat(it.size(), greaterThan(1L))
        }
    }

    private Map<String, String> getLastJobParameters(){
        Action[] actions = actionsCaptor.getValue()
        Map<String, String> entries = new TreeMap<>()
        actions.each { action ->
            if (action instanceof ParametersAction){
                entries.putAll(action.getParameters().collectEntries { [(it.name): it.value] })
            } else if (action instanceof SetEnvironmentVariablesAction){
                entries.putAll(action.getParametersAsMap())
            }
        }
        return entries
    }

    @Then('the log is')
    public void the_log_is(String expectedLog){
        String actualLog = logStream.toString().replaceAll(/\d{4}\/\d{2}\/\d{2} \d{2}:\d{2}:\d{2} (AM|PM|午前|午後)/, '<date>').replaceAll(/\r\n/, '\n')
		expectedLog = expectedLog.replaceAll(/\r\n/, '\n')
        assertThat(actualLog, is(expectedLog));
    }

    @Then('the response should be (OK|ERROR|WARNING) with message \'(.+)\'')
    public void i_should_see_the_following_text(String expectedValidationKind, String expectedMessage) throws Throwable {
        if (isNull(validation)){
            fail("Cannot validate response if you haven't invoked TestConnection first!")
        }
        assertThat(validation.kind, is(FormValidation.Kind.valueOf(expectedValidationKind)))
        assertThat(validation.message, containsString(expectedMessage))
    }
}
