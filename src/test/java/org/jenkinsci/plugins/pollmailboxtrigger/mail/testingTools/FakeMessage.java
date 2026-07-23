package org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * A configurable {@link Message} implementation for tests. All getters default to the
 * "empty message" behaviour (matching a freshly constructed message); individual values
 * can be set via the fluent {@code with*} methods.
 *
 * <p>This is the Java replacement for the old Groovy {@code NoopMessage}/{@code MessageBuilder}
 * test helpers.</p>
 */
public class FakeMessage extends Message {

    private Date sentDate;
    private Date receivedDate;
    private Address[] from = new Address[0];
    private Address[] recipients = new Address[0];
    private String subject;
    private Flags flags;
    private Folder messageFolder;
    private int number;
    private List<String> headers;
    private String contentType;
    private Object content;
    private String mimeTypePrefix;

    public FakeMessage withSentDate(final Date date) {
        this.sentDate = date;
        return this;
    }

    public FakeMessage withReceivedDate(final Date date) {
        this.receivedDate = date;
        return this;
    }

    public FakeMessage withFrom(final Address[] addresses) {
        this.from = addresses;
        return this;
    }

    public FakeMessage withRecipients(final Address[] addresses) {
        this.recipients = addresses;
        return this;
    }

    public FakeMessage withSubject(final String subject) {
        this.subject = subject;
        return this;
    }

    public FakeMessage withFlags(final Flags flags) {
        this.flags = flags;
        return this;
    }

    public FakeMessage withFolder(final Folder folder) {
        this.messageFolder = folder;
        return this;
    }

    public FakeMessage withMessageNumber(final int number) {
        this.number = number;
        return this;
    }

    public FakeMessage withHeaders(final List<String> headers) {
        this.headers = headers;
        return this;
    }

    public FakeMessage withContentType(final String contentType) {
        this.contentType = contentType;
        return this;
    }

    public FakeMessage withContent(final Object content) {
        this.content = content;
        return this;
    }

    /** {@link #isMimeType(String)} returns true when the queried type starts with this prefix. */
    public FakeMessage withMimeTypePrefix(final String prefix) {
        this.mimeTypePrefix = prefix;
        return this;
    }

    /* --- Message getters used by the production code --- */

    @Override
    public Address[] getFrom() {
        return from;
    }

    @Override
    public Address[] getAllRecipients() {
        return recipients;
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public Date getSentDate() {
        return sentDate;
    }

    @Override
    public Date getReceivedDate() {
        return receivedDate;
    }

    @Override
    public Flags getFlags() {
        return flags;
    }

    @Override
    public Folder getFolder() {
        return messageFolder;
    }

    @Override
    public int getMessageNumber() {
        return number;
    }

    @Override
    public Enumeration getAllHeaders() {
        return headers == null ? null : Collections.enumeration(headers);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public Object getContent() {
        return content;
    }

    @Override
    public boolean isMimeType(final String mimeType) {
        return mimeTypePrefix != null && mimeType.startsWith(mimeTypePrefix);
    }

    /* --- Remaining abstract methods: no-op / empty --- */

    @Override
    public void setFrom() {
    }

    @Override
    public void setFrom(final Address address) {
    }

    @Override
    public void addFrom(final Address[] addresses) {
    }

    @Override
    public Address[] getRecipients(final RecipientType type) {
        return new Address[0];
    }

    @Override
    public void setRecipients(final RecipientType type, final Address[] addresses) {
    }

    @Override
    public void addRecipients(final RecipientType type, final Address[] addresses) {
    }

    @Override
    public void setSubject(final String subject) {
    }

    @Override
    public void setSentDate(final Date date) {
    }

    @Override
    public void setFlags(final Flags flags, final boolean set) {
    }

    @Override
    public Message reply(final boolean replyToAll) {
        return null;
    }

    @Override
    public void saveChanges() {
    }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public int getLineCount() {
        return 0;
    }

    @Override
    public String getDisposition() {
        return null;
    }

    @Override
    public void setDisposition(final String disposition) {
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void setDescription(final String description) {
    }

    @Override
    public String getFileName() {
        return null;
    }

    @Override
    public void setFileName(final String filename) {
    }

    @Override
    public InputStream getInputStream() {
        return null;
    }

    @Override
    public DataHandler getDataHandler() {
        return null;
    }

    @Override
    public void setDataHandler(final DataHandler dataHandler) {
    }

    @Override
    public void setContent(final Object content, final String type) {
    }

    @Override
    public void setText(final String text) {
    }

    @Override
    public void setContent(final Multipart multipart) {
    }

    @Override
    public void writeTo(final OutputStream os) {
    }

    @Override
    public String[] getHeader(final String name) {
        return new String[0];
    }

    @Override
    public void setHeader(final String name, final String value) {
    }

    @Override
    public void addHeader(final String name, final String value) {
    }

    @Override
    public void removeHeader(final String name) {
    }

    @Override
    public Enumeration getMatchingHeaders(final String[] names) {
        return null;
    }

    @Override
    public Enumeration getNonMatchingHeaders(final String[] names) {
        return null;
    }
}
