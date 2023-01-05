package dslab.transfer;

import dslab.model.Host;
import dslab.model.Mail;
import dslab.nameserver.INameserverRemote;
import dslab.util.ValidationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class TransferMailSenderThread extends Thread {

    private final BlockingQueue<Mail> queue;
    private final INameserverRemote rootNameserverRemote;
    private boolean finished = false;

    public TransferMailSenderThread(BlockingQueue<Mail> queue, INameserverRemote rootNameserverRemote) {
        this.queue = queue;
        this.rootNameserverRemote = rootNameserverRemote;
    }

    @Override
    public void run() {
        try {
            while (!finished) {
                var mail = queue.take();
                // servers are all different domain endings we have to talk to..
                var servers = new HashSet<String>();
                for (String recipient : mail.getTo()) {
                    servers.add(recipient.split("@")[1]);
                }

                // iterate through all mailbox servers (gmail.com, yahoo.com ...)
                for (String server : servers) {
                    sendMessage(server, mail);
                }
            }
        } catch (InterruptedException ex) {
            // Take was interrupted
        }
    }

    public void sendMessage(String server, Mail mail) {
        Mail errorMail = null;
        var host = lookupMailboxServerAddress(server);
        if (host == null) {
            sendErrorMail(generateErrorEmail(mail, "error unknown host"), mail);
            return;
        }
        try (Socket socket = new Socket(host.ip, host.port)) {
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverWriter = new PrintWriter(socket.getOutputStream());

            String input = serverReader.readLine();
            if (!input.equals("ok DMTP")) throw new ValidationException("error protocol error");
            handleCommand("begin", "ok", serverReader, serverWriter);
            handleCommand("to " + String.join(",", mail.getTo()), "ok " + mail.getTo().size(), serverReader, serverWriter);
            handleCommand("subject " + mail.getSubject(), "ok", serverReader, serverWriter);
            handleCommand("from " + mail.getFrom(), "ok", serverReader, serverWriter);
            handleCommand("data " + mail.getData(), "ok", serverReader, serverWriter);
            handleCommand("send", "ok", serverReader, serverWriter);
            handleCommand("quit", "ok bye", serverReader, serverWriter);
        } catch (ConnectException e) {
            errorMail = generateErrorEmail(mail, "error cant connect to host " + server);
        } catch (IOException e) {
            errorMail = generateErrorEmail(mail, "error io exception");
        } catch (ValidationException e) {
            errorMail = generateErrorEmail(mail, e.getMessage());
        }

        // send error mail
        if (errorMail != null) {
            sendErrorMail(errorMail, mail);
        }
    }

    public void shutdown() {
        finished = true;
        interrupt();
    }

    private Mail generateErrorEmail(Mail originalMail, String errorMsg) {
        Mail mail = new Mail();
        mail.setSubject("error sending email with subject: " + originalMail.getSubject());
        mail.setTo(Set.of(new String[]{originalMail.getFrom()}));
        mail.setData(errorMsg);
        try {
            mail.setFrom("mailer@" + InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            // cant get local ip, just return null;
            return null;
        }
        return mail;
    }

    private void handleCommand(String command, String expectedResponse, BufferedReader serverReader, PrintWriter writer) throws ValidationException, IOException {
        writer.println(command);
        writer.flush();
        String input = serverReader.readLine();
        if (!input.equals(expectedResponse)) throw new ValidationException("error protocol error");
    }

    private void sendErrorMail(Mail errorMail, Mail originalMail) {
        String domain = originalMail.getFrom().split("@")[1];
        Host host = lookupMailboxServerAddress(domain);
        if (host == null) return;

        try (Socket socket = new Socket(host.ip, host.port)) {
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter serverWriter = new PrintWriter(socket.getOutputStream());

            String input = serverReader.readLine();
            if (!input.equals("ok DMTP")) throw new ValidationException("error protocol error");
            handleCommand("begin", "ok", serverReader, serverWriter);
            handleCommand("to " + String.join(",", errorMail.getTo()), "ok " + errorMail.getTo().size(), serverReader, serverWriter);
            handleCommand("subject " + errorMail.getSubject(), "ok", serverReader, serverWriter);
            handleCommand("from " + errorMail.getFrom(), "ok", serverReader, serverWriter);
            handleCommand("data " + errorMail.getData(), "ok", serverReader, serverWriter);
            handleCommand("send", "ok", serverReader, serverWriter);
            handleCommand("quit", "ok", serverReader, serverWriter);
        } catch (Exception e) {
            // discard
        }
    }

    private Host lookupMailboxServerAddress(String domain) {
        String[] subdomains = domain.split("\\.");
        INameserverRemote currentNameserver = rootNameserverRemote;
        try {
            int pos = subdomains.length - 1;
            while (pos > 0) {
                currentNameserver = currentNameserver.getNameserver(subdomains[pos]);
                if (currentNameserver == null) {
                    return null;
                }
                pos--;
            }
            String address = currentNameserver.lookup(subdomains[0]);
            return address != null ? Host.fromString(address) : null;
        } catch (RemoteException e) {
            return null;
        }
    }
}
