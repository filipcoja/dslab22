package dslab.mailbox;

import at.ac.tuwien.dsg.orvell.Shell;
import dslab.threads.HandlerThread;
import dslab.threads.ListenerThread;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * Thread for listening incoming requests from User
 */
public class MailboxDMAPListenerThread extends ListenerThread {
    private final MailboxServerDatabase mailboxServerDatabase;

    public MailboxDMAPListenerThread(ServerSocket serverSocket, Shell shell, MailboxServerDatabase mailboxServerDatabase) {
        super(serverSocket, shell);
        this.mailboxServerDatabase = mailboxServerDatabase;
    }

    @Override
    protected HandlerThread getHandlerThread(Socket socket) {
        return new MailboxDMAPHandlerThread(socket, mailboxServerDatabase, threads::remove);
    }
}
