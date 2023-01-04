package dslab.model;

public class Host {
    public String ip;
    public int port;

    public Host(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public static Host fromString(String address) throws NumberFormatException {
        String[] parts = address.split(":");
        return new Host(parts[0], Integer.parseInt(parts[1]));
    }

    @Override
    public int hashCode() {
        return ip.hashCode() + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Host host = (Host) o;
        return port == host.port && ip.equals(host.ip);
    }

    @Override
    public String toString() {
        return ip + ":" + port;
    }
}
