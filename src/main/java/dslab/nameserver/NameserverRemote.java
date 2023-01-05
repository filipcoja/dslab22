package dslab.nameserver;

import java.io.PrintStream;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NameserverRemote implements INameserverRemote {

    private final PrintStream outPrintStream;
    private final Map<String, INameserverRemote> subZones;
    private final Map<String, String > managedServers;

    public NameserverRemote(PrintStream outPrintStream) {
        this.outPrintStream = outPrintStream;

        this.subZones = new HashMap<>();
        this.managedServers = new HashMap<>();
    }

    @Override
    public void registerNameserver(String domain, INameserverRemote nameserver) throws RemoteException, AlreadyRegisteredException, InvalidDomainException {
       String[] subdomains = domain.split("\\.");
       if (subdomains.length == 1) {
           if (subZones.containsKey(subdomains[0])) {
               throw new AlreadyRegisteredException("Domain '" + domain + "' is already registered at this Nameserver!");
           }
           subZones.put(subdomains[0], nameserver);
           logMessage("Registering Nameserver for for Zone '" + subdomains[0] + "'");
       } else {
           INameserverRemote nextChildNameserver = this.getNameserver(subdomains[subdomains.length - 1]);
           if (nextChildNameserver == null) {
               throw new InvalidDomainException("The Domain: '" + subdomains[subdomains.length - 1] + "' is not registered at this Nameserver!");
           }
           nextChildNameserver.registerNameserver(String.join(".", Arrays.copyOf(subdomains, subdomains.length - 1)), nameserver);
       }
    }

    @Override
    public void registerMailboxServer(String domain, String address) throws RemoteException, AlreadyRegisteredException, InvalidDomainException {
        String[] subdomains = domain.split("\\.");
        if (subdomains.length == 1) {
            if (managedServers.containsKey(subdomains[0])) {
                throw new AlreadyRegisteredException("Mailserver '" + domain + "' is already registered at this Nameserver!");
            }
            managedServers.put(subdomains[0], address);
            logMessage("Registering MailboxServer '" + subdomains[0] + "': '" + address + "'");
        } else {
            INameserverRemote nextChildNameserver = this.getNameserver(subdomains[subdomains.length - 1]);
            if (nextChildNameserver == null) {
                throw new InvalidDomainException("The Domain: '" + subdomains[subdomains.length - 1] + "' is not registered at this Nameserver!");
            }
            nextChildNameserver.registerMailboxServer(String.join(".", Arrays.copyOf(subdomains, subdomains.length - 1)), address);
        }
    }

    @Override
    public INameserverRemote getNameserver(String zone) throws RemoteException {
        return subZones.get(zone);
    }

    @Override
    public String lookup(String domain) throws RemoteException {
        logMessage("Address for '" + domain + "' looked up by a TransferServer");
        return this.managedServers.get(domain);
    }

    public String[] getSubZones() {
        return this.subZones.keySet().toArray(new String[0]);
    }

    public String[] getMangedServers() {
        return this.managedServers.entrySet().stream().map(e -> "'" + e.getKey() + "': '" + e.getValue() + "'").toArray(String[]::new);
    }

    private void logMessage(String message) {
        Date date = new Date(System.currentTimeMillis());
        String dateString = new SimpleDateFormat("HH:mm:ss").format(date);
        outPrintStream.println(dateString + " : " + message);
    }
}
