package dslab.processors;

import dslab.mailbox.MailboxServerDatabase;
import dslab.model.HandshakeStatus;
import dslab.model.Mail;
import dslab.util.AESUtil;
import dslab.util.IntegrityUtil;
import dslab.util.PrivateKeyUtil;

public class DMAPCommandProcessor extends CommandProcessor {
    private final MailboxServerDatabase mailboxServerDatabase;

    private boolean loggedIn = false;
    private String usernameLoggedIn = "";

    private final PrivateKeyUtil privateKeyUtil;
    private final IntegrityUtil integrity = new IntegrityUtil();

    public DMAPCommandProcessor(MailboxServerDatabase mailboxServerDatabase) {
        super();
        this.mailboxServerDatabase = mailboxServerDatabase;
        privateKeyUtil = new PrivateKeyUtil("keys/server/" + mailboxServerDatabase.componentId + ".der");
    }

    @Override
    public void sendInit() {
        send("ok DMAP2.0");
    }

    @Override
    public boolean handleType(String type, String[] commandParts) {
        if (handshakeStatus == HandshakeStatus.AWAITING_CHALLENGE) {
            String payload = privateKeyUtil.decryptBase64(commandParts[0]);
            commandParts = payload.split(" ");
            return !commandParts[0].equals("ok") || handleHandshakeAwaitingOk(commandParts);
        } else if (handshakeStatus == HandshakeStatus.AWAITING_OK) {
            commandParts = aesUtil.decryptBase64(commandParts[0]).split(" ");
            if (!commandParts[0].equals("ok")) return true;
            handshakeStatus = HandshakeStatus.FINISHED;
        } else {
            if (handshakeStatus == HandshakeStatus.FINISHED) {
                commandParts = aesUtil.decryptBase64(commandParts[0]).split(" ");
                type = commandParts[0];
            }
            switch (type) {
                case "login":
                    handleLogin(commandParts);
                    break;
                case "list":
                    handleList();
                    break;
                case "show":
                    handleShow(commandParts);
                    break;
                case "delete":
                    handleDelete(commandParts);
                    break;
                case "logout":
                    handleLogout();
                    break;
                case "startsecure":
                    send("ok " + mailboxServerDatabase.componentId);
                    handshakeStatus = HandshakeStatus.AWAITING_CHALLENGE;
                    break;
                case "quit":
                    sendBye();
                    return true;
                default:
                    send("error protocol error");
                    return true;
            }
        }
        return false;
    }

    private boolean handleHandshakeAwaitingOk(String[] commandParts) {
        if (commandParts.length != 4) {
            return true;
        }
        String clientChallenge = commandParts[1];
        String secretKey = commandParts[2];
        String iv = commandParts[3];
        aesUtil = new AESUtil(secretKey, iv);
        send(aesUtil.encryptBase64("ok " + clientChallenge)); // client challenge hier decrypted angeben
        handshakeStatus = HandshakeStatus.AWAITING_OK;
        return false;
    }

    private void handleLogin(String[] commandParts) {
        if (commandParts.length != 3) {
            send("error malformed login command");
        } else if (loggedIn) {
            send("error first logout user");
        } else if (!mailboxServerDatabase.existsUser(commandParts[1])) {
            send("error unknown user");
        } else if (!mailboxServerDatabase.checkPassword(commandParts[1], commandParts[2])) {
            send("error wrong password");
        } else {
            // password was correct and user existed
            loggedIn = true;
            usernameLoggedIn = commandParts[1];
            sendOk();
        }
    }

    private void handleList() {
        if (!loggedIn) {
            send("error not logged in");
            return;
        }
        var mails = mailboxServerDatabase.getUserMails(usernameLoggedIn);
        if (mails.keySet().size() == 0) {
            writer.println(handshakeStatus == HandshakeStatus.FINISHED ? this.aesUtil.encryptBase64("no messages") : "no messages");
        } else {
            for (int id : mails.keySet()) {
                Mail mail = mails.get(id);
                String mailString = id + " " + mail.getFrom() + " " + mail.getSubject();
                if (handshakeStatus == HandshakeStatus.FINISHED) mailString = this.aesUtil.encryptBase64(mailString);
                writer.println(mailString);
            }
        }
        writer.println(handshakeStatus == HandshakeStatus.FINISHED ? this.aesUtil.encryptBase64("ok") : "ok");

        writer.flush();
    }

    private void handleShow(String[] commandParts) {
        if (!loggedIn) {
            send("error not logged in");
            return;
        } else if (commandParts.length != 2) {
            send("error malformed show command");
            return;
        }

        try {
            int id = Integer.parseInt(commandParts[1]);
            Mail mail = mailboxServerDatabase.getUserMail(usernameLoggedIn, id);
            if (mail != null) {
                send("from " + mail.getFrom());
                send("to " + String.join(",", mail.getTo()));
                send("subject " + mail.getSubject());
                send("data " + mail.getData());
                send("hash " + mail.getHash());
                send("ok");
            } else {
                send("error mail not found");
            }
        } catch (NumberFormatException e) {
            send("error id is not an integer");
        }
    }

    private void handleDelete(String[] commandParts) {
        if (!loggedIn) {
            send("error not logged in");
            return;
        } else if (commandParts.length != 2) {
            send("error malformed delete command");
            return;
        }

        try {
            int id = Integer.parseInt(commandParts[1]);
            Mail mail = mailboxServerDatabase.deleteUserMail(usernameLoggedIn, id);
            if (mail != null) {
                sendOk();
            } else {
                send("error mail not found");
            }
        } catch (NumberFormatException e) {
            send("error id is not an integer");
        }
    }

    private void handleLogout() {
        if (loggedIn) {
            loggedIn = false;
            sendOk();
        } else {
            send("error cant logout if no one is logged in");
        }
    }
}
