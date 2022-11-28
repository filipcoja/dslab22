package dslab.transfer;

import dslab.model.Mail;
import dslab.threads.HandlerThread;
import dslab.processors.CommandProcessor;
import dslab.processors.TransferDMTPCommandProcessor;
import dslab.util.Config;

import javax.xml.crypto.Data;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;

/**
 * Thread for listening incoming requests from User
 */
public class TransferDMAPHandlerThread extends HandlerThread {
    private final BlockingQueue<Mail> queue;
    private DatagramSocket udpSocket;
    private final int dmtpPort;

    private InetAddress monitorAddress;
    private final int monitorPort;

    public TransferDMAPHandlerThread(Socket socket, BlockingQueue<Mail> queue, Config config, Consumer<HandlerThread> callback) {
        super(socket, callback);
        dmtpPort = config.getInt("tcp.port");
        try {
            monitorAddress = InetAddress.getByName(config.getString("monitoring.host"));
        } catch (Exception ignored) {}
        monitorPort = config.getInt("monitoring.port");

        try {
            udpSocket = new DatagramSocket();
        } catch (SocketException ignored) {}
        this.queue = queue;
    }

    @Override
    protected CommandProcessor getCommandProcessor() {
        return new TransferDMTPCommandProcessor(queue, udpSocket, dmtpPort, monitorAddress, monitorPort);
    }

    @Override
    protected void shutdown(boolean calledByMyself) {
        if (udpSocket != null && !udpSocket.isClosed()) {
            udpSocket.close();
        }
        super.shutdown(calledByMyself);
    }
}
