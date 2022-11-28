package dslab.monitoring;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.DatagramSocket;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.util.Config;

public class MonitoringServer implements IMonitoringServer {

    private final Shell shell;
    private MonitoringListenerThread monitoringListenerThread;
    private final MonitoringServerDatabase monitoringServerDatabase;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config      the component config
     * @param in          the input stream to read console input from
     * @param out         the output stream to write console output to
     */
    public MonitoringServer(String componentId, Config config, InputStream in, PrintStream out) {
        shell = new Shell(in, out);
        shell.register(this);
        shell.setPrompt(componentId + "> ");

        shell.out().println("Started Init Process");
        monitoringServerDatabase = new MonitoringServerDatabase();
        try {
            // constructs a datagram socket and binds it to the specified port
            var datagramSocket = new DatagramSocket(config.getInt("udp.port"));

            // create a new thread to listen for incoming packets
            monitoringListenerThread = new MonitoringListenerThread(datagramSocket, monitoringServerDatabase, shell);
            monitoringListenerThread.start();
            shell.out().println("Started ListenerThread");
        } catch (IOException e) {
            shell.out().println("Cant listen on UDP port" + config.getInt("udp.port"));
        }

    }

    @Override
    public void run() {
        shell.run();
    }

    @Command
    @Override
    public void addresses() {
        monitoringServerDatabase.printUserCount(shell);
    }

    @Override
    @Command
    public void servers() {
        monitoringServerDatabase.printHostCount(shell);
    }

    @Override
    @Command
    public void shutdown() {
        monitoringListenerThread.shutdown();
        throw new StopShellException();
    }

    public static void main(String[] args) throws Exception {
        IMonitoringServer server = ComponentFactory.createMonitoringServer(args[0], System.in, System.out);
        server.run();
    }

}
