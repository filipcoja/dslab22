package dslab.transfer;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.model.Mail;
import dslab.nameserver.INameserverRemote;
import dslab.util.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TransferServer implements ITransferServer, Runnable {
    private final Shell shell;

    private TransferDMTPListenerThread transferDMTPListenerThread;
    private TransferMailSenderThread transferMailSenderThread;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public TransferServer(String componentId, Config config, InputStream in, PrintStream out) {
        shell = new Shell(in, out);
        shell.register(this);
        shell.setPrompt(componentId + "> ");

        shell.out().println("Started Init Process");

        // Start new dmap and dmtp thread for handling incoming requests from clients and transfer servers
        try {
            ServerSocket dmtpSocket = new ServerSocket(config.getInt("tcp.port"));
            shell.out().println("Started ServerSockets");

            // FIFO Prinzip
            BlockingQueue<Mail> mailSendingQueue = new LinkedBlockingQueue<>();
            // Start all three threads for (Listening to Connections, Worker for Mail sending queue)
            transferDMTPListenerThread = new TransferDMTPListenerThread(dmtpSocket, mailSendingQueue, config, shell);
            transferDMTPListenerThread.start();
            INameserverRemote rootNameserverRemote = getRootNameserverRemote(config);
            transferMailSenderThread = new TransferMailSenderThread(mailSendingQueue, rootNameserverRemote);
            transferMailSenderThread.start();
            shell.out().println("Started DMTPListener and MailSenderThread");
        } catch (RemoteException | NotBoundException e) {
            shell.out().println("Couldn't create Remotes: " + e.getMessage());
        } catch (IOException e) {
            shell.out().println("Error: Couldn't create server sockets");
        }
    }

    @Override
    public void run() {
        shell.run();
    }

    @Override
    @Command
    public void shutdown() {
        if (transferDMTPListenerThread != null) transferDMTPListenerThread.shutdown();
        if (transferMailSenderThread != null) transferMailSenderThread.shutdown();
        throw new StopShellException();
    }

    private INameserverRemote getRootNameserverRemote(Config config) throws RemoteException, NotBoundException {
        Registry rootRegistry = LocateRegistry.getRegistry(config.getString("registry.host"), config.getInt("registry.port"));
        return (INameserverRemote) rootRegistry.lookup(config.getString("root_id"));
    }

    public static void main(String[] args) throws Exception {
        ITransferServer server = ComponentFactory.createTransferServer(args[0], System.in, System.out);
        server.run();
    }

}
