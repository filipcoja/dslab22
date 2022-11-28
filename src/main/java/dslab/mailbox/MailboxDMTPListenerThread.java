package dslab.mailbox;

import at.ac.tuwien.dsg.orvell.Shell;
import dslab.threads.HandlerThread;
import dslab.threads.ListenerThread;
import dslab.transfer.TransferDMAPHandlerThread;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Thread for listening incoming requests from User
 */
public class MailboxDMTPListenerThread extends ListenerThread {
    private final MailboxServerDatabase mailboxServerDatabase;

    public MailboxDMTPListenerThread(ServerSocket serverSocket, Shell shell, MailboxServerDatabase mailboxServerDatabase) {
        super(serverSocket, shell);
        this.mailboxServerDatabase = mailboxServerDatabase;
    }

    @Override
    protected HandlerThread getHandlerThread(Socket socket) {
        return new MailboxDMTPHandlerThread(socket, mailboxServerDatabase, threads::remove);
    }
}
