package dslab.client;

import dslab.util.AESUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

public class ClientSocketHandler {
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private AESUtil aesUtil;

    public ClientSocketHandler(Socket socket, AESUtil aesUtil) throws IOException {
        this.socket = socket;
        this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.writer = new PrintWriter(this.socket.getOutputStream());
        this.aesUtil = aesUtil;
    }

    public String[] sendMessageAndReceiveMessages(String message) {
        sendMessage(message);

        return receiveMessages();
    }

    public String[] sendMessageAndReceiveEncryptedMessages(String message) {
        sendMessage(message);

        return receiveEncryptedMessages();
    }

    public String[] sendEncryptedMessageAndReceiveEncryptedMessages(String message) {
        sendEncryptedMessage(message);

        return receiveEncryptedMessages();
    }

    public void sendMessage(String message) {
        this.writer.println(message);
        this.writer.flush();
    }

    public void sendEncryptedMessage(String message) {
        this.writer.println(this.aesUtil.encryptBase64(message));
        this.writer.flush();
    }

    public String[] receiveMessages() {
        List<String> lines = new LinkedList<>();
        String s;
        try {
            while (!(s = reader.readLine()).startsWith("ok") && !s.startsWith("error")) {
                lines.add(s);
            }
            lines.add(s);
        } catch (IOException ignored) {}

        return lines.toArray(String[]::new);
    }

    public String[] receiveEncryptedMessages() {
        List<String> lines = new LinkedList<>();
        String s;
        try {
            while (!(s = this.aesUtil.decryptBase64(reader.readLine())).startsWith("ok") && !s.startsWith("error")) {
                lines.add(s);
            }
            lines.add(s);
        } catch (IOException ignored) {}

        return lines.toArray(String[]::new);
    }

    public void close() {
        if (socket == null) {
            return;
        }
        try {
            this.socket.close();
        } catch (IOException ignored) {}
    }
}
