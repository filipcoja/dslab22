package dslab.mailbox;

import dslab.threads.HandlerThread;
import dslab.processors.CommandProcessor;
import dslab.processors.DMAPCommandProcessor;

import java.net.Socket;
import java.util.function.Consumer;

/**
 * Thread for listening incoming requests from User
 */
public class MailboxDMAPHandlerThread extends HandlerThread {
    private final MailboxServerDatabase mailboxServerDatabase;

    public MailboxDMAPHandlerThread(Socket socket, MailboxServerDatabase mailboxServerDatabase, Consumer<HandlerThread> callback) {
        super(socket, callback);
        this.mailboxServerDatabase = mailboxServerDatabase;
    }

    @Override
    protected CommandProcessor getCommandProcessor() {
        return new DMAPCommandProcessor(mailboxServerDatabase);
    }
}
