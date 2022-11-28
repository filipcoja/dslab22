package dslab.monitoring;

import at.ac.tuwien.dsg.orvell.Shell;
import dslab.model.Host;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitoringServerDatabase {
    private final ConcurrentHashMap<String, Integer> userToCount;
    private final ConcurrentHashMap<Host, Integer> hostToCount;

    public MonitoringServerDatabase() {
        this.userToCount = new ConcurrentHashMap<>();
        this.hostToCount = new ConcurrentHashMap<>();
    }

    public synchronized void incrementUserCount(String username) {
        if (userToCount.containsKey(username)) {
            userToCount.put(username, userToCount.get(username) + 1);
        } else {
            userToCount.put(username, 1);
        }
    }

    public synchronized void incrementHostCount(Host host) {
        if (hostToCount.containsKey(host)) {
            hostToCount.put(host, hostToCount.get(host) + 1);
        } else {
            hostToCount.put(host, 1);
        }
    }

    public void printUserCount(Shell shell) {
        for (String user : userToCount.keySet()) {
            shell.out().println(user + " " + userToCount.get(user));
        }
    }

    public void printHostCount(Shell shell) {
        for (Host host : hostToCount.keySet()) {
            shell.out().println(host + " " + hostToCount.get(host));
        }
    }
}
