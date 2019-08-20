package org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools

import org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Stringify

import javax.mail.Address
import javax.mail.Flags
import javax.mail.Message
import javax.mail.Multipart
import javax.mail.Part
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMultipart
import java.text.SimpleDateFormat

import static javax.mail.Flags.Flag

/**
 * Created by nickgrealy@gmail.com on 17/10/14.
 */
class MessageBuilder {

    static final DEFAULT_DATE = new Date(1413196250440)
    static final DEFAULT_DATE_FORMATTED = new SimpleDateFormat(Stringify.DATE_FORMAT_TEXT).format(DEFAULT_DATE)

    static final TEXT = '''
fruit=banana
veg=carrot
email=foobar@abc.com

fruit2=banana
veg2=carrot
email2=foobar@abc.com

abc=0123456789
def=!@#$%^&*()
ghi=[]\\;',./{}|:"<>?

--
Kind regards,

Nick
'''
    static final def HTML = '''
<div dir="ltr"><div style="font-family:arial,sans-serif;font-size:13px"><font color="#990000" face="verdana, sans-serif">fruit=banana<br></font></div><div style="font-family:arial,sans-serif;font-size:13px"><div><font color="#990000" face="verdana, sans-serif">veg=carrot</font></div><div><font color="#990000" face="verdana, sans-serif">email=<a href="mailto:foobar@abc.com" target="_blank">foobar@abc.com</a></font></div></div><div><br></div><div><div>fruit2=banana</div><div>veg2=carrot</div><div>email2=<a href="mailto:foobar@abc.com">foobar@abc.com</a></div></div><div><br></div><div>abc=0123456789</div><div>def=!@#$%^&amp;*()</div><div>ghi=[]\\;&#39;,./{}|:&quot;&lt;&gt;?</div><div><br></div>-- <br>Kind regards,<br><br><div>Nick</div>
</div>

'''
    public static final String DEFAULT_SUBJECT = 'foobar!!!'

    /**
     * Builds a simple email Message.
     * @param subject
     * @return
     */
    public static Message buildMessage(String subject = DEFAULT_SUBJECT, Date sentReceivedDate = DEFAULT_DATE) {
        buildMessageCandidate(subject, sentReceivedDate) as NoopMessage
    }

    public static Message buildMessage(
            String subject = DEFAULT_SUBJECT,
            int sentXMinutesAgo,
            boolean isSeenFlag,
            String from,
            String body,
            String attachments
    ) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, -1 * sentXMinutesAgo);
        Date date = cal.getTime();
        buildMessageCandidate(subject, date, [isSeenFlag ? Flag.SEEN : Flag.RECENT], from, body, attachments) as NoopMessage
    }

    /**
     * Utility method, for building a map of methods used by candidate objects, who implement the Message interface.
     * @param subject
     * @return
     */
    private static Map buildMessageCandidate(
            String subject = DEFAULT_SUBJECT,
            Date sentReceivedDate = DEFAULT_DATE,
            List<Flag> flags = [Flag.ANSWERED, Flag.DRAFT],
            String from = 'foo1@bar.com,foo2@bar.com',
            String body = 'aaa=bbb\nfoo=<b>bar</b>',
            String attachments = ''
    ) {
        List<String> fromAddr = from ? from.split(',').collect() : []
        List<String> files = attachments ? attachments.split(',').collect() : []
        [
                getSentDate     : { sentReceivedDate },
                getFrom         : { fromAddr.collect { new InternetAddress(it) } as Address[] },
                getSubject      : { subject },
                getFlags        : {
                    def tmp = new Flags()
                    flags.each { tmp.add(it) }
                    tmp
                },
                getFolder       : { [getFullName: { 'Drafts/Foobar' }] as NoopFolder },
                getMessageNumber: { 1337 },
                getReceivedDate : { sentReceivedDate },
                getAllHeaders   : { Collections.enumeration(['Foo', 'Bar']) },
                getContentType  : { 'text/html' },
                getAllRecipients: { ['foo3@bar.com', 'foo4@bar.com'].collect { new InternetAddress(it) } as Address[] },
                getContent      : { files.isEmpty() ? body.toString() : buildMultipartContent(files) },
                isMimeType      : { it.startsWith('text') || it.startsWith("multipart")}
        ]
    }


    def static Message buildMultipartMessage() {
        // build multipart message
        MimeBodyPart part1 = new MimeBodyPart();
        MimeBodyPart part2 = new MimeBodyPart();
        part1.setText(TEXT, "TEXT/PLAIN; charset=UTF-8");
        part2.setContent(HTML, "TEXT/HTML; charset=UTF-8");
        def multiPart = new MimeMultipart("ALTERNATIVE")
        multiPart.addBodyPart(part1)
        multiPart.addBodyPart(part2)
        def candidate = buildMessageCandidate()
        candidate['getContent'] = { multiPart }
        candidate['isMimeType'] = { it.startsWith('multipart') }
        candidate['getContentType'] = { multiPart.getContentType() }
        candidate as NoopMessage
    }

    public static Multipart buildMultipartContent(List<String> files){
        Multipart multipart = new MimeMultipart()
        files.each {
            def part = new MimeBodyPart(MessageBuilder.getClassLoader().getResourceAsStream(it))
            if (!it.startsWith("message_rfc822")){
                part.setFileName(it)
            }
            part.setDisposition(Part.ATTACHMENT)
            multipart.addBodyPart(part)
        }
        return multipart
    }
}
