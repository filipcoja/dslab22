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

    public NameserverRemote(PrintStream outPrintStream) {
        this.outPrintStream = outPrintStream;

        this.subZones = new HashMap<>();
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
           INameserverRemote parentNameserver = this.getNameserver(subdomains[subdomains.length - 1]);
           if (parentNameserver == null) {
               throw new InvalidDomainException("The Domain: '" + subdomains[subdomains.length - 1] + "' is not registered at this Nameserver!");
           }
           parentNameserver.registerNameserver(String.join(".", Arrays.copyOf(subdomains, subdomains.length - 1)), nameserver);
       }
    }

    @Override
    public void registerMailboxServer(String domain, String address) throws RemoteException, AlreadyRegisteredException, InvalidDomainException {

    }

    @Override
    public INameserverRemote getNameserver(String zone) throws RemoteException {
        return subZones.get(zone);
    }

    @Override
    public String lookup(String domain) throws RemoteException {
        return null;
    }

    public String[] getSubZones() {
        return this.subZones.keySet().toArray(new String[0]);
    }

    private void logMessage(String message) {
        Date date = new Date(System.currentTimeMillis());
        String dateString = new SimpleDateFormat("HH:mm:ss").format(date);
        outPrintStream.println(dateString + " : " + message);
    }
}
