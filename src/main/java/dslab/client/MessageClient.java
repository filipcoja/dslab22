package dslab.client;

import at.ac.tuwien.dsg.orvell.Shell;
import at.ac.tuwien.dsg.orvell.StopShellException;
import at.ac.tuwien.dsg.orvell.annotation.Command;
import dslab.ComponentFactory;
import dslab.model.Mail;
import dslab.util.AESUtil;
import dslab.util.Config;
import dslab.util.PublicKeyUtil;

import java.io.InputStream;
import java.io.PrintStream;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Set;

public class MessageClient implements IMessageClient, Runnable {

    private Config config;
    private ClientSocketHandler dmtpSocketHandler;
    private ClientSocketHandler dmapSocketHandler;
    private Shell shell;
    private AESUtil aesUtil = new AESUtil();


    /**
     * Creates a new client instance.
     *
     * @param componentId the id of the component that corresponds to the Config resource
     * @param config the component config
     * @param in the input stream to read console input from
     * @param out the output stream to write console output to
     */
    public MessageClient(String componentId, Config config, InputStream in, PrintStream out) {
        this.shell = new Shell(in, out);
        this.shell.register(this);
        this.shell.setPrompt(componentId + "> ");

        this.shell.out().println("Started Init Process");
        this.config = config;
    }

    @Override
    public void run() {
        this.beginDMAP();
        this.shell.run();
    }

    private boolean beginDMAP() {
        this.dmapSocketHandler = new ClientSocketHandler(this.config.getString("mailbox.host"), this.config.getInt("mailbox.port"), this.aesUtil, this.shell);
        if (!dmapSocketHandler.isConnected()) {
            return false;
        }
        String[] reply = this.dmapSocketHandler.receiveMessages();
        if (reply.length < 1 || !reply[0].equals("ok DMAP2.0")) {
            this.shell.err().println("DMAP error");
            return false;
        }
        reply = this.dmapSocketHandler.sendMessageAndReceiveMessages("startsecure");
        if (reply.length < 1 || !reply[0].startsWith("ok") || !reply[0].contains(" ")) {
            this.shell.err().println("DMAP error");
            return false;
        }
        String challenge = generateClientChallenge();
        PublicKeyUtil publicKeyUtil = new PublicKeyUtil("keys/client/" + reply[0].split(" ")[1] + "_pub.der");
        String msg = publicKeyUtil.encryptBase64(String.format("ok %s %s %s", challenge, this.aesUtil.getSecret(), this.aesUtil.getIv()));
        reply = this.dmapSocketHandler.sendMessageAndReceiveEncryptedMessages(msg);
        if (reply.length < 1 || !reply[0].startsWith("ok") || !reply[0].contains(" ")) {
            this.shell.err().println("DMAP error");
            return false;
        }
        if (!challenge.equals(reply[0].split(" ")[1])) {
            this.shell.err().println("Encryption error");
            return false;
        }
        this.dmapSocketHandler.sendEncryptedMessage("ok");
        reply = this.dmapSocketHandler.sendEncryptedMessageAndReceiveEncryptedMessages(String.format("login %s %s", this.config.getString("mailbox.user"), this.config.getString("mailbox.password")));
        if (reply.length < 1 || !reply[0].equals("ok")) {
            this.shell.err().println("DMAP login error");
            return false;
        }
        return true;
    }

    @Override
    @Command
    public void inbox() {
        if (!dmapServerAvailable()) {
            this.shell.out().println("DMAP server is currently not available");
            return;
        }
        String[] list = this.dmapSocketHandler.sendEncryptedMessageAndReceiveEncryptedMessages("list");
        StringBuilder stringBuilder = new StringBuilder();
        if (list.length < 1 || list[0].equals("no messages")) {
            this.shell.out().println("no messages");
            return;
        }
        for (int i = 0; i < list.length - 1; i++) {
            String mailId = list[i].split(" ")[0];
            stringBuilder.append("=== " + mailId + " ===").append("\n");
            String[] message = this.dmapSocketHandler.sendEncryptedMessageAndReceiveEncryptedMessages("show " + mailId);
            stringBuilder.append(String.join("\n", Arrays.copyOf(message, message.length - 1)));
            if (i != list.length - 2) {
                stringBuilder.append("\n");
            }
        }
        this.shell.out().println(stringBuilder);
    }

    @Override
    @Command
    public void delete(String id) {
        if (!dmapServerAvailable()) {
            this.shell.out().println("DMAP server is currently not available");
            return;
        }
        if (id == null || id.trim().length() == 0) {
            this.shell.err().println("error no message-id given");
            return;
        }

        String[] message = this.dmapSocketHandler.sendEncryptedMessageAndReceiveEncryptedMessages("delete " + id);
        if (message.length < 1 || !message[0].startsWith("ok")) {
            this.shell.out().println("Error deleting mail");
            return;
        }
        this.shell.out().println("Deleted mail");
    }

    @Override
    @Command
    public void verify(String id) {
        if (!dmapServerAvailable()) {
            this.shell.out().println("DMAP server is currently not available");
            return;
        }
        if (id == null || id.trim().length() == 0) {
            this.shell.err().println("error no message-id given");
            return;
        }

        String[] message = this.dmapSocketHandler.sendEncryptedMessageAndReceiveEncryptedMessages("show " + id);
        if (message.length != 6) {
            return;
        }

        Mail mail = new Mail();
        mail.setFrom(message[0].split(" ", 2)[1]);
        mail.setTo(Set.of(message[1].split(" ", 2)[1]));
        mail.setSubject(message[2].split(" ", 2)[1]);
        mail.setData(message[3].split(" ", 2)[1]);
        mail.setHash();

        if (message[4].split(" ")[1].equals("null")) {
            this.shell.out().println("Integrity could not be verified");
        }else if (mail.getHash().equals(message[4].split(" ")[1])) {
            this.shell.out().println("Integrity OK");
        } else {
            this.shell.out().println("Integrity not OK");
        }
    }

    private boolean dmapServerAvailable() {
        return this.dmapSocketHandler.isConnected() || beginDMAP();
    }

    @Override
    @Command
    public void msg(String to, String subject, String data) {
        this.dmtpSocketHandler = new ClientSocketHandler(this.config.getString("transfer.host"), this.config.getInt("transfer.port"), this.aesUtil, this.shell);
        if (!this.dmtpSocketHandler.isConnected()) {
            this.shell.out().println("Cannot send messages at the moment, try again later.");
            return;
        }
        if (
            to == null || to.trim().length() == 0
            || subject == null || subject.trim().length() == 0
            || data == null || data.trim().length() == 0
        ) {
            this.shell.err().println("error invalid input. Usage: msg <to> \"<subject>\" \"<data>\"");
            return;
        }

        Mail mail = new Mail();
        mail.setData(data);
        mail.setSubject(subject);
        mail.setTo(Set.of(to));
        mail.setFrom(this.config.getString("transfer.email"));
        mail.setHash();

        if (
            communicate("begin")
            && communicate("from " + mail.getFrom())
            && communicate("subject " + mail.getSubject())
            && communicate("to " + String.join(",", mail.getTo()))
            && communicate("data " + mail.getData())
            && communicate("hash " + mail.getHash())
            && communicate("send")
        ) {
            this.shell.out().println("Successfully sent message");
        } else {
            this.shell.out().println("Error sending message");
        }
        this.dmtpSocketHandler.close();
    }

    private boolean communicate(String message) {
        String[] reply = this.dmtpSocketHandler.sendMessageAndReceiveMessages(message);
        return reply.length >= 1 && reply[0].startsWith("ok");
    }

    @Override
    @Command
    public void shutdown() {
        this.shell.out().println("Shutting down client ...");
        this.dmtpSocketHandler.close();
        this.dmapSocketHandler.close();
        throw new StopShellException();
    }

    public static String generateClientChallenge() {
        byte[] clientChallenge = new byte[32];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(clientChallenge);
        return Base64.getEncoder().encodeToString(clientChallenge);
    }

    public static void main(String[] args) throws Exception {
        IMessageClient client = ComponentFactory.createMessageClient(args[0], System.in, System.out);
        client.run();
    }
}
