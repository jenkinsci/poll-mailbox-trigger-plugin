package org.jenkinsci.plugins.pollmailboxtrigger.mail.utils;

import com.google.common.io.Files;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;
import javax.mail.search.SearchTerm;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.jenkinsci.plugins.pollmailboxtrigger.PollMailboxTrigger.Properties.subjectContains;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.SearchTermHelpers.and;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.SearchTermHelpers.flag;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.SearchTermHelpers.not;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.SearchTermHelpers.receivedSince;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Stringify.BLANK;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Stringify.MULTIPART_WILDCARD;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Stringify.NEWLINE;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Stringify.stringify;

/**
 * @author Nick Grealy
 */
@SuppressWarnings("unused")
public abstract class MailWrapperUtils {

    /**
     * Utility class for providing extra Messaging related methods!
     */
    public static class MessagesWrapper extends Logger.HasLogger {

        private List<Message> messages;
        private Folder folder;

        public MessagesWrapper(final Logger logger, final List<Message> messages, final Folder folder) {
            super(logger);
            this.messages = messages;
            this.folder = folder;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public Folder getFolder() {
            return folder;
        }

        public MessagesWrapper print() throws MessagingException {
            getLogger().info("Found message(s) : " + messages.size());
            for (Message message : messages) {
                getLogger().info(">>>>>>");
                getLogger().info("Date    : " + message.getSentDate());
                getLogger().info("From    : " + (message.getFrom().length > 0 ? message.getFrom()[0] : null));
                getLogger().info("Subject : " + message.getSubject());
                getLogger().info("<<<<<<");
            }
            return this;
        }

        public MessagesWrapper markAsRead(final Message... messagez) throws IOException, MessagingException {
            return markAsRead(Arrays.asList(messagez));
        }

        public MessagesWrapper markAsRead(final List<Message> messagez) throws IOException, MessagingException {
            if (folder.isOpen() && folder.getMode() != Folder.READ_WRITE) {
                folder.close(true);
            }
            if (!folder.isOpen()) {
                folder.open(Folder.READ_WRITE);
            }
            for (Message message : messagez) {
                message.setFlag(Flags.Flag.SEEN, true);
            }
            getLogger().info("Marked email(s) as read : " + messagez.size());
            return this;
        }

        public MessagesWrapper markAsRead() throws IOException, MessagingException {
            return markAsRead(messages);
        }

        public CustomProperties getMessageProperties(final Message message, final String prefix, final CustomProperties p) throws MessagingException, IOException {
            CustomProperties envVars = new CustomProperties();
            String msgSubject = stringify(message.getSubject());
            envVars.put(prefix + "subject", (msgSubject == null) ? "" : msgSubject);
            envVars.put(prefix + "from", stringify(message.getFrom()));
            envVars.put(prefix + "replyTo", stringify(message.getReplyTo()));
            envVars.put(prefix + "flags", stringify(message.getFlags()));
            envVars.put(prefix + "folder", stringify(message.getFolder()));
            envVars.put(prefix + "messageNumber", stringify(message.getMessageNumber()));
            envVars.put(prefix + "receivedDate", stringify(message.getReceivedDate()));
            envVars.put(prefix + "sentDate", stringify(message.getSentDate()));
            envVars.put(prefix + "headers", stringify(message.getAllHeaders()));
            envVars.put(prefix + "content", stringify(message));
            envVars.put(prefix + "contentType", stringify(message.getContentType()));
            envVars.put(prefix + "recipients", stringify(message.getAllRecipients()));
            // add parameters from email content
            final CustomProperties properties = CustomProperties.read(getText(message));
            envVars.putAll(properties);
//            envVars.put(prefix + "emailParams", stringify(properties.getMap(), "=", "&"));
            // add "jobTrigger"
            if (p.has(subjectContains)) {
                String subject = p.get(subjectContains);
                // normalise strings, find index, then get text after it
                int idx = msgSubject.toLowerCase().indexOf(subject.toLowerCase());
                int beginIndex = idx + subject.length();
                if (idx > -1 && beginIndex < msgSubject.length()) {
                    envVars.put(prefix + "jobTrigger", msgSubject.substring(beginIndex).trim());
                }
            }
            return envVars;
        }

        public static String getText(final Part p) {
            /* I could probably do this all in one line, with groovy. :( */
            try {
                // get all text from the email body...
                String text = stringify(p);
                // turn BR, P, DIV into newlines
                text = stringify(text.split("(?i)</?br[^>]*>|</?p[^>]*>|</?div[^>]*>"), NEWLINE);
                // strip out any remaining HTML tags
                text = stringify(text.split("<[^>]*>"), BLANK);
                // only keep lines with a "=" sign...
                final List<String> content = filterProperties(text.split(NEWLINE), "=");
                // join lines back up into a single string...
                text = stringify(content, NEWLINE);
                return text;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /*
         * Only keep text containing the given string.
         */
        public static List<String> filterProperties(final String[] props, final String containing) {
            final ArrayList<String> list = new ArrayList<String>(Arrays.asList(props));
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String foo = it.next();
                if (!foo.contains(containing)) {
                    it.remove();
                }
            }
            return list;
        }

        /**
         * Saves all attachments to a temp directory, and returns the directory path. Null if no attachments.
         */
        public File saveAttachments(final Message message) throws IOException, MessagingException {
            File tmpDir = Files.createTempDir();
            boolean foundAttachments = false;
            Object content = message.getContent();
            if (message.isMimeType(MULTIPART_WILDCARD) && content instanceof Multipart) {
                Multipart mp = (Multipart) content;
                for (int i = 0; i < mp.getCount(); i++) {
                    BodyPart bodyPart = mp.getBodyPart(i);
                    if (bodyPart instanceof MimeBodyPart && isNotBlank(bodyPart.getFileName())) {
                        MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;
                        mimeBodyPart.saveFile(new File(tmpDir, mimeBodyPart.getFileName()));
                        foundAttachments = true;
                    }
                }
            }
            return foundAttachments ? tmpDir : null;
        }

        public void close() throws MessagingException {
            folder.close(true);
        }
    }

    /*
     * Utility class for providing extra Folder related methods!
     */
    public static class FolderWrapper extends Logger.HasLogger {

        public static final SearchTerm UNREAD_FLAG = not(flag(Flags.Flag.SEEN));

        private Folder folder;

        public FolderWrapper(final Logger logger, final Folder folder) {
            super(logger);
            this.folder = folder;
        }

        public Folder getFolder() {
            return folder;
        }

        public MessagesWrapper getUnreadMessagesSince(final Date fromDate) throws MessagingException {
            return search(UNREAD_FLAG, receivedSince(fromDate));
        }

        public MessagesWrapper getUnreadMessages() throws MessagingException {
            return search(UNREAD_FLAG);
        }

        public MessagesWrapper search(final SearchTerm... term) throws MessagingException {
            return search(Arrays.asList(term));
        }

        public MessagesWrapper search(final List<SearchTerm> term) throws MessagingException {
            if (!folder.isOpen()) {
                folder.open(Folder.READ_ONLY);
            }
            Message[] messages = folder.search(and(term));
            return new MessagesWrapper(getLogger(), Arrays.asList(messages), folder);
        }

        public void close() throws MessagingException {
            folder.close(true);
        }

    }
}
