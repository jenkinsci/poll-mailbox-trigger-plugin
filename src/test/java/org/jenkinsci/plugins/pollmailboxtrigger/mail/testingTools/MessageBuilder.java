package org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools;

import org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Stringify;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Builds the fake email {@link Message}s used across the tests.
 * Java replacement for the old Groovy {@code MessageBuilder}.
 */
public final class MessageBuilder {

    public static final Date DEFAULT_DATE = new Date(1413196250440L);
    public static final String DEFAULT_DATE_FORMATTED =
            new SimpleDateFormat(Stringify.DATE_FORMAT_TEXT).format(DEFAULT_DATE);
    public static final String DEFAULT_SUBJECT = "foobar!!!";

    static final String TEXT = "\n"
            + "fruit=banana\n"
            + "veg=carrot\n"
            + "email=foobar@abc.com\n"
            + "\n"
            + "fruit2=banana\n"
            + "veg2=carrot\n"
            + "email2=foobar@abc.com\n"
            + "\n"
            + "abc=0123456789\n"
            + "def=!@#$%^&*()\n"
            + "\n"
            + "--\n"
            + "Kind regards,\n"
            + "\n"
            + "Nick\n";

    static final String HTML = "\n"
            + "<div dir=\"ltr\">"
            + "<div>fruit=banana<br></div>"
            + "<div>veg=carrot</div>"
            + "<div>email=<a href=\"mailto:foobar@abc.com\">foobar@abc.com</a></div>"
            + "<div>fruit2=banana</div>"
            + "<div>veg2=carrot</div>"
            + "<div>email2=<a href=\"mailto:foobar@abc.com\">foobar@abc.com</a></div>"
            + "<div>abc=0123456789</div>"
            + "<div>def=!@#$%^&amp;*()</div>"
            + "-- <br>Kind regards,<br><br><div>Nick</div>\n"
            + "</div>\n";

    private MessageBuilder() {
    }

    public static Message buildMessage() {
        return buildMessage(DEFAULT_SUBJECT);
    }

    public static Message buildMessage(final String subject) {
        return baseMessage(subject)
                .withContentType("text/html")
                .withContent("aaa=bbb\nfoo=<b>bar</b>")
                .withMimeTypePrefix("text");
    }

    public static Message buildMultipartMessage() throws MessagingException {
        MimeBodyPart part1 = new MimeBodyPart();
        MimeBodyPart part2 = new MimeBodyPart();
        part1.setText(TEXT, "UTF-8", "plain");
        part2.setContent(HTML, "text/html; charset=UTF-8");
        MimeMultipart multiPart = new MimeMultipart("alternative");
        multiPart.addBodyPart(part1);
        multiPart.addBodyPart(part2);
        return baseMessage(DEFAULT_SUBJECT)
                .withContentType(multiPart.getContentType())
                .withContent(multiPart)
                .withMimeTypePrefix("multipart");
    }

    private static FakeMessage baseMessage(final String subject) {
        return new FakeMessage()
                .withSubject(subject)
                .withSentDate(DEFAULT_DATE)
                .withReceivedDate(DEFAULT_DATE)
                .withMessageNumber(1337)
                .withFrom(addresses("foo1@bar.com", "foo2@bar.com"))
                .withRecipients(addresses("foo3@bar.com", "foo4@bar.com"))
                .withFolder(new FakeFolder("Drafts/Foobar"))
                .withFlags(flags(Flags.Flag.ANSWERED, Flags.Flag.DRAFT))
                .withHeaders(new ArrayList<>(Arrays.asList("Foo", "Bar")));
    }

    public static Address[] addresses(final String... emails) {
        List<Address> list = new ArrayList<>();
        for (String email : emails) {
            try {
                list.add(new InternetAddress(email));
            } catch (MessagingException e) {
                throw new RuntimeException(e);
            }
        }
        return list.toArray(new Address[0]);
    }

    public static Flags flags(final Flags.Flag... flagz) {
        Flags flags = new Flags();
        for (Flags.Flag flag : flagz) {
            flags.add(flag);
        }
        return flags;
    }
}
