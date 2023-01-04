package dslab.processors;

import dslab.mailbox.MailboxServerDatabase;
import dslab.model.Mail;

public class DMAPCommandProcessor extends CommandProcessor {
    private final MailboxServerDatabase mailboxServerDatabase;

    private boolean loggedIn = false;
    private String usernameLoggedIn = "";

    public DMAPCommandProcessor(MailboxServerDatabase mailboxServerDatabase) {
        super();
        this.mailboxServerDatabase = mailboxServerDatabase;
    }

    @Override
    public void sendInit() {
        send("ok DMAP");
    }

    @Override
    public boolean handleType(String type, String[] commandParts) {
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
            case "quit":
                sendBye();
                return true;
            default:
                send("error protocol error");
                return true;
        }
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
            writer.println("no messages");
        } else {
            for (int id : mails.keySet()) {
                Mail mail = mails.get(id);
                writer.println(id + " " + mail.getFrom() + " " + mail.getSubject());
            }
        }

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
                writer.println("from " + mail.getFrom());
                writer.println("to " + String.join(",", mail.getTo()));
                writer.println("subject " + mail.getSubject());
                writer.println("data " + mail.getData());
                writer.flush();
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
