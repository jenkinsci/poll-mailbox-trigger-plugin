package org.jenkinsci.plugins.pollmailboxtrigger;

import hudson.util.Secret;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Smoke tests that exercise the plugin inside a real Jenkins instance. These verify that the
 * (xtrigger-lib based) {@link PollMailboxTrigger} extension loads correctly against the configured
 * Jenkins baseline.
 */
@WithJenkins
class PollMailboxTriggerJenkinsTest {

    @Test
    void descriptorIsRegistered(JenkinsRule j) {
        PollMailboxTrigger.PollMailboxTriggerDescriptor descriptor =
                (PollMailboxTrigger.PollMailboxTriggerDescriptor)
                        j.jenkins.getDescriptorOrDie(PollMailboxTrigger.class);
        assertNotNull(descriptor);
        assertEquals("Poll Mailbox Trigger", descriptor.getDisplayName());
        assertEquals("/plugin/poll-mailbox-trigger-plugin/help-PollMailboxTrigger.html",
                descriptor.getHelpFile());
    }

    @Test
    void triggerCanBeConstructed(JenkinsRule j) throws Exception {
        PollMailboxTrigger trigger = new PollMailboxTrigger(
                "H/5 * * * *", null, false,
                "imap.example.com", "user", Secret.fromString("secret"), "", "IGNORE");

        assertEquals("imap.example.com", trigger.getHost());
        assertEquals("user", trigger.getUsername());
        assertEquals("H/5 * * * *", trigger.getSpec());
    }
}
