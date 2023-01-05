package dslab.mailbox;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.nameserver.AlreadyRegisteredException;
import dslab.nameserver.INameserverRemote;
import dslab.nameserver.InvalidDomainException;
import dslab.util.Config;

public class MailboxServer implements IMailboxServer, Runnable {
    private final Shell shell;

    private MailboxDMAPListenerThread mailboxDmapListenerThread;
    private MailboxDMTPListenerThread mailboxDmtpListenerThread;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config      the component config
     * @param in          the input stream to read console input from
     * @param out         the output stream to write console output to
     */
    public MailboxServer(String componentId, Config config, InputStream in, PrintStream out) {
        shell = new Shell(in, out);
        shell.register(this);
        shell.setPrompt(componentId + "> ");

        shell.out().println("Started Init Process");
        var mailboxServerDatabase = new MailboxServerDatabase(config);


        // Start new dmap and dmtp thraed for handling incoming requests from clients and transfer servers
        try {
            ServerSocket dmapSocket = new ServerSocket(config.getInt("dmap.tcp.port"));
            ServerSocket dmtpSocket = new ServerSocket(config.getInt("dmtp.tcp.port"));
            shell.out().println("Started ServerSockets");

            mailboxDmapListenerThread = new MailboxDMAPListenerThread(dmapSocket, shell, mailboxServerDatabase);
            mailboxDmtpListenerThread = new MailboxDMTPListenerThread(dmtpSocket, shell, mailboxServerDatabase);
            mailboxDmapListenerThread.start();
            mailboxDmtpListenerThread.start();
            shell.out().println("Started DMTPListener and DMAPListener");

            registerAtNameserver(config);
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
        mailboxDmapListenerThread.shutdown();
        mailboxDmtpListenerThread.shutdown();
        throw new StopShellException();
    }

    private void registerAtNameserver(Config config) {
        try {
            Registry rootRegistry = LocateRegistry.getRegistry(config.getString("registry.host"), config.getInt("registry.port"));
            INameserverRemote rootNameserverRemote = (INameserverRemote)rootRegistry.lookup(config.getString("root_id"));
            String address = InetAddress.getLocalHost().getHostAddress() + ":" + config.getInt("dmtp.tcp.port");
            rootNameserverRemote.registerMailboxServer(config.getString("domain"), address);
        } catch (RemoteException | NotBoundException | UnknownHostException e) {
            shell.out().println("Couldn't create Remotes to register Mailbox-Server at Nameserver: " + e.getMessage());
            return;
        } catch (AlreadyRegisteredException | InvalidDomainException e) {
            shell.out().println("Error while registering Mailbox-Server at Nameserver: " + e.getMessage());
            return;
        }
        shell.out().println("Registered Mailbox-Server at Nameserver");
    }

    public static void main(String[] args) throws Exception {
        IMailboxServer server = ComponentFactory.createMailboxServer(args[0], System.in, System.out);
        server.run();
    }
}
