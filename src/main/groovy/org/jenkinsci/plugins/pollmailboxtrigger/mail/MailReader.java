package org.jenkinsci.plugins.pollmailboxtrigger.mail;

import org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.CustomProperties;
import org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Logger;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.ArrayList;
import java.util.List;

import static org.jenkinsci.plugins.pollmailboxtrigger.PollMailboxTrigger.STORE_IMAPS;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Logger.HasLogger;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.MailWrapperUtils.FolderWrapper;

/**
 * Allows us to read mail using the imap protocol.
 *
 * @author Nick Grealy
 */
@SuppressWarnings("unused")
public class MailReader extends HasLogger {

    private String host, storeName = STORE_IMAPS, username, password;
    private CustomProperties properties = new CustomProperties();
    private Folder currentFolder;
    private Store store;

    public MailReader(
            final String host,
            final String username,
            final String password
    ) throws MessagingException {
        this(host, username, password, STORE_IMAPS, Logger.getDefault(), new CustomProperties());
    }

    public MailReader(
            final String host,
            final String username,
            final String password,
            final String storeName,
            final Logger logger,
            final CustomProperties properties
    ) throws MessagingException {
        super(logger);
        this.host = host;
        this.storeName = storeName;
        this.username = username;
        this.password = password;
        this.properties = properties;
    }

    public MailReader connect() throws MessagingException {
        Thread.currentThread().setContextClassLoader(Session.class.getClassLoader());
        Session session = Session.getInstance(properties.getProperties(), null);
        session.setDebugOut(getLogger().getPrintStream());
        store = session.getStore(storeName);
        store.connect(host, username, password);
        getLogger().info("[Poll Mailbox Trigger] - Connected!");
        return this;
    }

    public FolderWrapper folder(final String folderName) throws MessagingException {
        if (store == null) {
            throw new RuntimeException("Session is not connected!");
        }
        currentFolder = store.getFolder(folderName);
        return new FolderWrapper(getLogger(), currentFolder);
    }

    public List<String> getFolders() throws MessagingException {
        javax.mail.Folder[] folders = store.getDefaultFolder().list("*");
        List<String> folderNames = new ArrayList<String>();
        for (javax.mail.Folder folder : folders) {
            if ((folder.getType() & javax.mail.Folder.HOLDS_MESSAGES) != 0) {
                folderNames.add(folder.getFullName());
            }
        }
        return folderNames;
    }

    public void close() {
        if (currentFolder != null && currentFolder.isOpen()) {
            try {
                currentFolder.close(true);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
        if (store != null) {
            try {
                store.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }
}
