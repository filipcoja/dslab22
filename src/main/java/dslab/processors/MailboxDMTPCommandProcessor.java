package dslab.processors;

import dslab.mailbox.MailboxServerDatabase;
import dslab.model.Mail;

import java.util.Arrays;
import java.util.HashSet;

public class MailboxDMTPCommandProcessor extends DMTPCommandProcessor {
    private final MailboxServerDatabase mailboxServerDatabase;

    public MailboxDMTPCommandProcessor(MailboxServerDatabase mailboxServerDatabase) {
        super();
        this.mailboxServerDatabase = mailboxServerDatabase;
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

        // check if recipients are emails and exist
        String[] recipients = commandParts[1].split(",");
        for (String recipient : recipients) {
            if (!emailRegex.matcher(recipient).matches()) {
                send("error one of the recpients is not an email adress");
                return;
            }

            String[] emailParts = recipient.split("@");
            String username = emailParts[0];
            String domain = emailParts[1];
            if (domain.equals(mailboxServerDatabase.domain) && !mailboxServerDatabase.existsUser(username)) {
                send("error unknown recipient " + username);
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
                mailboxServerDatabase.addMailToRecepients(mail);
                mail = new Mail();
                begin = false;
                sendOk();
                break;
        }
    }
}
