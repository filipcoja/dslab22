package dslab.mailbox;

import dslab.processors.CommandProcessor;
import dslab.processors.MailboxDMTPCommandProcessor;
import dslab.threads.HandlerThread;

import java.net.Socket;
import java.util.function.Consumer;

/**
 * Thread for listening incoming requests from User
 */
public class MailboxDMTPHandlerThread extends HandlerThread {
    private final MailboxServerDatabase mailboxServerDatabase;

    public MailboxDMTPHandlerThread(Socket socket, MailboxServerDatabase mailboxServerDatabase, Consumer<HandlerThread> callback) {
        super(socket, callback);
        this.mailboxServerDatabase = mailboxServerDatabase;
    }

    @Override
    protected CommandProcessor getCommandProcessor() {
        return new MailboxDMTPCommandProcessor(mailboxServerDatabase);
    }
}
