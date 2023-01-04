package dslab.transfer;

import at.ac.tuwien.dsg.orvell.Shell;
import dslab.model.Mail;
import dslab.threads.HandlerThread;
import dslab.threads.ListenerThread;
import dslab.util.Config;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/**
 * Thread for listening incoming requests from User
 */
public class TransferDMTPListenerThread extends ListenerThread {
    private final BlockingQueue<Mail> queue;
    private final Config config;

    public TransferDMTPListenerThread(ServerSocket serverSocket, BlockingQueue<Mail> queue, Config config, Shell shell) {
        super(serverSocket, shell);
        this.queue = queue;
        this.config = config;
    }

    @Override
    protected HandlerThread getHandlerThread(Socket socket) {
        return new TransferDMTPHandlerThread(socket, queue, config, threads::remove);
    }
}
