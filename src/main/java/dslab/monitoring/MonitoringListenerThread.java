package dslab.monitoring;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import dslab.model.Host;
import dslab.threads.ListenerThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Thread for listening incoming requests from User
 */
public class MonitoringListenerThread extends Thread {
    private final DatagramSocket datagramSocket;
    private final MonitoringServerDatabase monitoringServerDatabase;
    private final Shell shell;

    public MonitoringListenerThread(DatagramSocket datagramSocket, MonitoringServerDatabase monitoringServerDatabase, Shell shell) {
        this.datagramSocket = datagramSocket;
        this.monitoringServerDatabase = monitoringServerDatabase;
        this.shell = shell;
    }

    public void run() {
        byte[] buffer;
        DatagramPacket packet;
        try {
            while (true) {
                buffer = new byte[1024];
                packet = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(packet);
                String request = new String(packet.getData(), 0, packet.getLength());
                String[] reqParts = request.split(" ");
                try {
                    var host = Host.fromString(reqParts[0]);
                    monitoringServerDatabase.incrementHostCount(host);
                    monitoringServerDatabase.incrementUserCount(reqParts[1]);
                } catch (NumberFormatException e) {
                    // ignore this request, malformed..
                }
            }
        } catch (SocketException e) {
            // when the socket is closed, the send or receive methods of the DatagramSocket will throw a SocketException
//            System.out.println("SocketException while waiting for/handling packets: " + e.getMessage());
        } catch (IOException e) {
            shell.out().println("Error: IO Exception while handling UDP Sockets");
        } finally {
            shutdown();
        }
    }

    public void shutdown() {
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
        }
    }
}
