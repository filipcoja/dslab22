package dslab.nameserver;

import java.io.InputStream;
import java.io.PrintStream;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.util.Config;

public class Nameserver implements INameserver {

    private final Shell shell;
    private final NameserverRemote nameserverRemote;
    private final boolean isRootNameserver;
    private Registry rootRegistry;
    private final String rootId;

    /**
     * Creates a new server instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public Nameserver(String componentId, Config config, InputStream in, PrintStream out) {
        shell = new Shell(in, out);
        shell.register(this);
        shell.setPrompt(componentId + "> ");

        rootId = config.getString("root_id");
        String domain = config.containsKey("domain") ? config.getString("domain") : null;
        int registryPort = config.getInt("registry.port");
        String registryHost = config.getString("registry.host");
        isRootNameserver = domain == null;

        nameserverRemote = new NameserverRemote(shell.out());

        try {
            if (isRootNameserver) {
                rootRegistry = LocateRegistry.createRegistry(registryPort);
                Remote remote = UnicastRemoteObject.exportObject(nameserverRemote, 0);
                rootRegistry.bind(rootId, remote);
            } else {
                rootRegistry = LocateRegistry.getRegistry(registryHost, registryPort);
                INameserverRemote rootNameserverRemote = (INameserverRemote)rootRegistry.lookup(rootId);
                UnicastRemoteObject.exportObject(nameserverRemote, 0);
                rootNameserverRemote.registerNameserver(domain, nameserverRemote);
            }
        } catch (RemoteException | AlreadyBoundException | NotBoundException | InvalidDomainException e) {
            shell.out().println("Couldn't create Remotes: " + e.getMessage());
        } catch (AlreadyRegisteredException e) {
            shell.out().println("Error: " + e.getMessage());
        }

    }

    @Override
    public void run() {
        shell.run();
    }

    @Override
    @Command
    public void nameservers() {
        String[] subZones = nameserverRemote.getSubZones();
        for (int i = 0; i < subZones.length; i++) {
            shell.out().println((i + 1) + ". " + subZones[i]);
        }
    }

    @Override
    @Command
    public void addresses() {
        // TODO
    }

    @Override
    @Command
    public void shutdown() {
        try {
            UnicastRemoteObject.unexportObject(nameserverRemote, true);
            if (isRootNameserver) {
                rootRegistry.unbind(rootId);
            }
        } catch (RemoteException | NotBoundException e) {
            shell.out().println("Error while closing Remotes!");
        }
        throw new StopShellException();
    }

    public static void main(String[] args) throws Exception {
        INameserver component = ComponentFactory.createNameserver(args[0], System.in, System.out);
        component.run();
    }

}
