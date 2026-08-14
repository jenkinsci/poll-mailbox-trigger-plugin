package org.jenkinsci.plugins.pollmailboxtrigger.mail.testingTools;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

/**
 * A no-op {@link Folder}, used purely to satisfy method signatures in tests.
 * The only meaningful behaviour is {@link #getFullName()}.
 */
public class FakeFolder extends Folder {

    private final String fullName;

    public FakeFolder() {
        this(null);
    }

    public FakeFolder(final String fullName) {
        super(newStore());
        this.fullName = fullName;
    }

    private static Store newStore() {
        try {
            // An unconnected IMAP store, purely to satisfy the Folder(Store) constructor.
            return Session.getInstance(new Properties()).getStore("imap");
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return fullName;
    }

    @Override
    public String getFullName() {
        return fullName;
    }

    @Override
    public Folder getParent() {
        return null;
    }

    @Override
    public boolean exists() {
        return false;
    }

    @Override
    public Folder[] list(final String pattern) {
        return new Folder[0];
    }

    @Override
    public char getSeparator() {
        return '/';
    }

    @Override
    public int getType() {
        return HOLDS_MESSAGES;
    }

    @Override
    public boolean create(final int type) {
        return false;
    }

    @Override
    public boolean hasNewMessages() {
        return false;
    }

    @Override
    public Folder getFolder(final String name) {
        return null;
    }

    @Override
    public boolean delete(final boolean recurse) {
        return false;
    }

    @Override
    public boolean renameTo(final Folder folder) {
        return false;
    }

    @Override
    public void open(final int mode) {
        // no-op
    }

    @Override
    public void close(final boolean expunge) {
        // no-op
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public Flags getPermanentFlags() {
        return null;
    }

    @Override
    public int getMessageCount() {
        return 0;
    }

    @Override
    public Message getMessage(final int msgnum) {
        return null;
    }

    @Override
    public void appendMessages(final Message[] msgs) {
        // no-op
    }

    @Override
    public Message[] expunge() throws MessagingException {
        return new Message[0];
    }
}
