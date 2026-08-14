package org.jenkinsci.plugins.pollmailboxtrigger;

import hudson.util.FormValidation;
import hudson.util.Secret;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.CustomProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link PollMailboxTrigger} that do not require a running Jenkins.
 *
 * <p>{@link SafeJenkins#useNativeInstance(boolean)} is toggled off so that the static
 * {@code Jenkins.getInstanceOrNull()} calls (node properties, {@link Secret} encryption)
 * are stubbed out.</p>
 */
class PollMailboxTriggerTest {

    private static final String IGNORE = PollMailboxTrigger.AttachmentOptions.IGNORE.name();

    @BeforeEach
    void useStubbedJenkins() {
        SafeJenkins.useNativeInstance(false);
    }

    @AfterEach
    void restoreJenkins() {
        SafeJenkins.useNativeInstance(true);
    }

    @Test
    void initialiseDefaultsForImaps() {
        CustomProperties properties = PollMailboxTrigger.initialiseDefaults(
                "imap.gmail.com", "me@gmail.com", Secret.fromString("secret"), "", IGNORE);
        Map<String, String> map = properties.getMap();

        assertEquals("imaps", map.get("storeName"));
        assertEquals("INBOX", map.get("folder"));
        assertEquals("jenkins >", map.get("subjectContains"));
        assertEquals("imap.gmail.com", map.get("host"));
        assertEquals("me@gmail.com", map.get("username"));
        assertEquals("1440", map.get("receivedXMinutesAgo"));
        assertEquals("imap.gmail.com", map.get("mail.imaps.host"));
        assertEquals("993", map.get("mail.imaps.port"));
        assertEquals("false", map.get("mail.debug"));
    }

    @Test
    void initialiseDefaultsHonoursUserSuppliedScript() {
        String script = "storeName=pop3\nfolder=Archive\nsubjectContains=build me";
        CustomProperties properties = PollMailboxTrigger.initialiseDefaults(
                "mail.example.com", "user", Secret.fromString("secret"), script, IGNORE);
        Map<String, String> map = properties.getMap();

        assertEquals("pop3", map.get("storeName"));
        assertEquals("Archive", map.get("folder"));
        assertEquals("build me", map.get("subjectContains"));
        assertEquals("110", map.get("mail.pop3.port"));
        // pop3 does not support received-date filtering
        assertFalse(map.containsKey("receivedXMinutesAgo"));
    }

    @Test
    void testConnectionReportsMissingRequiredProperties() {
        PollMailboxTrigger.PollMailboxTriggerDescriptor descriptor =
                new PollMailboxTrigger.PollMailboxTriggerDescriptor();

        FormValidation validation =
                descriptor.doTestConnection("", "", Secret.fromString(""), "", IGNORE);

        assertEquals(FormValidation.Kind.ERROR, validation.kind);
        assertTrue(validation.getMessage().contains("required"),
                "Expected validation message to mention required properties, was: " + validation.getMessage());
    }

    @Test
    void testConnectionSupportsJobStartTestMode() {
        PollMailboxTrigger.PollMailboxTriggerDescriptor descriptor =
                new PollMailboxTrigger.PollMailboxTriggerDescriptor();

        FormValidation validation = descriptor.doTestConnection(
                PollMailboxTrigger.TEST_JOB_START_MODE, "user", Secret.fromString("secret"), "", IGNORE);

        assertEquals(FormValidation.Kind.OK, validation.kind);
        assertTrue(validation.getMessage().contains(PollMailboxTrigger.TEST_JOB_START_MODE),
                "Expected the test-job-start marker, was: " + validation.getMessage());
    }
}
