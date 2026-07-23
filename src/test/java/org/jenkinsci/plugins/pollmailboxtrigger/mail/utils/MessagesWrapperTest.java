package org.jenkinsci.plugins.pollmailboxtrigger.mail.utils;

import org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.FakeFolder;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.FakeMessage;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.MessageBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.MessageBuilder.DEFAULT_DATE;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.MessageBuilder.DEFAULT_DATE_FORMATTED;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.MessageBuilder.buildMessage;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools.MessageBuilder.buildMultipartMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Port of the original Groovy {@code MessagesWrapperTest}.
 */
class MessagesWrapperTest {

    private StringBuilder sb;
    private Logger logger;
    private final Folder folder = new FakeFolder();
    private final List<Message> messages = new ArrayList<>();
    private MailWrapperUtils.MessagesWrapper wrapper;
    private CustomProperties properties;

    @BeforeEach
    void setup() {
        sb = new StringBuilder();
        logger = new Logger() {
            @Override
            public void info(final String message) {
                sb.append(message).append("\n");
            }

            @Override
            public void error(final String message) {
                sb.append(message).append("\n");
            }
        };
        wrapper = new MailWrapperUtils.MessagesWrapper(logger, messages, folder);
        properties = new CustomProperties();
    }

    @Test
    void printingNoMessages() throws MessagingException {
        wrapper.print();
        assertEquals("Found message(s) : 0\n", sb.toString());
    }

    @Test
    void printingOneMessage() throws MessagingException {
        messages.add(buildMessage());
        wrapper.print();
        assertEquals("Found message(s) : 1\n"
                + ">>>>>>\n"
                + "Date    : " + DEFAULT_DATE + "\n"
                + "From    : foo1@bar.com\n"
                + "Subject : foobar!!!\n"
                + "<<<<<<\n", sb.toString());
    }

    @Test
    void printingOneMessageWithNullValues() throws MessagingException {
        messages.add(new FakeMessage());
        wrapper.print();
        assertEquals("Found message(s) : 1\n"
                + ">>>>>>\n"
                + "Date    : null\n"
                + "From    : null\n"
                + "Subject : null\n"
                + "<<<<<<\n", sb.toString());
    }

    @Test
    void printingMultipleMessages() throws MessagingException {
        for (int i = 1; i <= 3; i++) {
            messages.add(buildMessage("foobar" + i));
        }
        wrapper.print();
        StringBuilder expected = new StringBuilder("Found message(s) : 3\n");
        for (int i = 1; i <= 3; i++) {
            expected.append(">>>>>>\n")
                    .append("Date    : ").append(DEFAULT_DATE).append("\n")
                    .append("From    : foo1@bar.com\n")
                    .append("Subject : foobar").append(i).append("\n")
                    .append("<<<<<<\n");
        }
        assertEquals(expected.toString(), sb.toString());
    }

    @Test
    void getMessagePropertiesWithNullValues() throws Exception {
        Map<String, String> props = wrapper.getMessageProperties(new FakeMessage(), "a_", properties).getMap();
        assertNull(props.get("a_subject"));
        assertEquals("", props.get("a_from"));
        assertEquals("", props.get("a_replyTo"));
        assertEquals("", props.get("a_recipients"));
        assertEquals("0", props.get("a_messageNumber"));
        assertEquals("null", props.get("a_flags"));
        assertEquals("null", props.get("a_folder"));
        assertEquals("null", props.get("a_receivedDate"));
        assertEquals("null", props.get("a_sentDate"));
        assertEquals("null", props.get("a_headers"));
        assertEquals("null", props.get("a_content"));
        assertNull(props.get("a_contentType"));
    }

    @Test
    void getMessagePropertiesWithValidValues() throws Exception {
        Map<String, String> props = wrapper.getMessageProperties(buildMessage(), "a_", properties).getMap();
        assertEquals("\naaa=bbb\nfoo=<b>bar</b>", props.get("a_content"));
        assertEquals("text/html", props.get("a_contentType"));
        assertEquals("ANSWERED, DRAFT", props.get("a_flags"));
        assertEquals("Drafts/Foobar", props.get("a_folder"));
        assertEquals("foo1@bar.com,foo2@bar.com", props.get("a_from"));
        assertEquals("Foo, Bar", props.get("a_headers"));
        assertEquals("1337", props.get("a_messageNumber"));
        assertEquals(DEFAULT_DATE_FORMATTED, props.get("a_receivedDate"));
        assertEquals("foo3@bar.com,foo4@bar.com", props.get("a_recipients"));
        assertEquals("foo1@bar.com,foo2@bar.com", props.get("a_replyTo"));
        assertEquals(DEFAULT_DATE_FORMATTED, props.get("a_sentDate"));
        assertEquals("foobar!!!", props.get("a_subject"));
        // properties parsed out of the email body
        assertEquals("bbb", props.get("aaa"));
        assertEquals("bar", props.get("foo"));
    }

    @Test
    void getMessagePropertiesFromMultipartMessage() throws Exception {
        Map<String, String> props = wrapper.getMessageProperties(buildMultipartMessage(), "a_", properties).getMap();
        // standard headers
        assertEquals("foobar!!!", props.get("a_subject"));
        assertEquals("foo1@bar.com,foo2@bar.com", props.get("a_from"));
        // properties parsed out of both the text and html body parts
        assertEquals("banana", props.get("fruit"));
        assertEquals("carrot", props.get("veg"));
        assertEquals("foobar@abc.com", props.get("email"));
        assertEquals("banana", props.get("fruit2"));
        assertEquals("carrot", props.get("veg2"));
        assertEquals("foobar@abc.com", props.get("email2"));
        assertEquals("0123456789", props.get("abc"));
        assertEquals("!@#$%^&*()", props.get("def"));
    }

    @Test
    void saveAttachments() throws Exception {
        MimeBodyPart attachment = new MimeBodyPart();
        attachment.attachFile(new File(getClass().getResource("/aurora.jpg").toURI()));
        attachment.setFileName("aurora.jpg");
        MimeMultipart multipart = new MimeMultipart();
        multipart.addBodyPart(attachment);
        Message message = new FakeMessage()
                .withContent(multipart)
                .withMimeTypePrefix("multipart");

        File dir = wrapper.saveAttachments(message);
        assertTrue(dir.isDirectory());
        File saved = new File(dir, "aurora.jpg");
        assertTrue(saved.isFile());
        assertTrue(saved.length() > 1L);
    }
}
