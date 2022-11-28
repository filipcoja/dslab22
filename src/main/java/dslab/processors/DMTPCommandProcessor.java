package dslab.processors;

import dslab.model.Mail;

import java.util.regex.Pattern;

public abstract class DMTPCommandProcessor extends CommandProcessor {
    protected boolean begin = false;
    protected Mail mail = new Mail();

    protected final Pattern emailRegex = Pattern.compile("^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$");

    public DMTPCommandProcessor() {
        super();
    }

    @Override
    public void sendInit() {
        send("ok DMTP");
    }

    @Override
    public boolean handleType(String type, String[] commandParts) {
        switch (type) {
            case "begin":
                handleBegin(commandParts);
                break;
            case "to":
                handleTo(commandParts);
                break;
            case "from":
                handleFrom(commandParts);
                break;
            case "subject":
                handleSubject(commandParts);
                break;
            case "data":
                handleData(commandParts);
                break;
            case "send":
                handleSend();
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

    private void handleBegin(String[] commandParts) {
        if (commandParts.length != 1) {
            send("error malformed begin command");
        } else if (begin) {
            mail.reset();
            sendOk();
        } else {
            begin = true;
            sendOk();
        }
    }


    private void handleFrom(String[] commandParts) {
        if (!begin) {
            send("error message not started");
            return;
        } else if (commandParts.length != 2) {
            send("error malformed from command");
            return;
        } else if (!emailRegex.matcher(commandParts[1]).matches()) {
            send("error address from is not an email");
            return;
        }

        mail.setFrom(commandParts[1]);
        sendOk();
    }

    private void handleSubject(String[] commandParts) {
        if (!begin) {
            send("error message not started");
            return;
        } else if (commandParts.length <= 1) {
            send("error malformed subject command");
            return;
        }

        String concatenated = commandParts[1];
        for (int i = 2; i < commandParts.length; i++) {
            concatenated += " " + commandParts[i];
        }
        mail.setSubject(concatenated);
        sendOk();
    }

    private void handleData(String[] commandParts) {
        if (!begin) {
            send("error message not started");
            return;
        } else if (commandParts.length <= 1) {
            send("error malformed data command");
            return;
        }

        String concatenated = commandParts[1];
        for (int i = 2; i < commandParts.length; i++) {
            concatenated += " " + commandParts[i];
        }
        mail.setData(concatenated);
        sendOk();
    }

    abstract protected void handleSend();

    abstract protected void handleTo(String[] commandParts);

}
