package dslab.processors;

import dslab.model.Mail;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;

public class TransferDMTPCommandProcessor extends DMTPCommandProcessor {
    private final BlockingQueue<Mail> queue;

    private final DatagramSocket udpSocket;

    private final InetAddress monitorAddress;
    private final int monitorPort;
    private final int dmtpPort;

    public TransferDMTPCommandProcessor(BlockingQueue<Mail> queue, DatagramSocket udpSocket, int dmtpPort, InetAddress monitorAddress, int monitorPort) {
        super();
        this.queue = queue;
        this.udpSocket = udpSocket;
        this.monitorAddress = monitorAddress;
        this.monitorPort = monitorPort;
        this.dmtpPort = dmtpPort;
    }

    @Override
    protected void handleTo(String[] commandParts) {
        if (!begin) {
            send("error message not started");
            return;
        } else if (commandParts.length != 2) {
            send("error malformed to command");
            return;
        }

        // check if recipients are emails
        String[] recipients = commandParts[1].split(",");
        for (String recipient : recipients) {
            if (!emailRegex.matcher(recipient).matches()) {
                send("error one of the recipients is not an email address");
                return;
            }
        }
        mail.setTo(new HashSet<>(Arrays.asList(recipients)));
        send("ok " + mail.getTo().size());
    }

    @Override
    protected void handleSend() {
        if (!begin) {
            send("error message not started");
            return;
        }

        var status = mail.getMailStatus();
        switch (status) {
            case MISSING_TO:
                send("error no recipients");
                break;
            case MISSING_FROM:
                send("error no sender");
                break;
            case MISSING_SUBJECT:
                send("error no subject");
                break;
            case MISSING_DATA:
                send("error no data");
                break;
            case OK:
                // transmit to blocking queue etc. // notify
                queue.add(mail);
                try {
                    String input = InetAddress.getLocalHost().getHostAddress() + ":" + dmtpPort + " " + mail.getFrom();
                    byte[] buffer = input.getBytes();
                    var packet = new DatagramPacket(buffer, buffer.length, monitorAddress, monitorPort);

                    // send request-packet to server
                    udpSocket.send(packet);
                }  catch (IOException e) {
                   // couldnt send packet.. just ignoring
                }
                mail = new Mail();
                begin = false;
                sendOk();
                break;
        }
    }
}
