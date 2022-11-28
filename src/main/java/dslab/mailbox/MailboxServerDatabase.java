package dslab.mailbox;

import dslab.util.Config;
import dslab.model.Mail;
import dslab.model.UserData;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class MailboxServerDatabase {
    // user => {id => Mail, id2 => Mail}
    private final HashMap<String, UserData> users;
    public final String domain;

    public MailboxServerDatabase(Config config) {
        var userConf = new Config(config.getString("users.config"));
        users = new HashMap<>();
        // Create entry for every user
        for (String username : userConf.listKeys()) {
            users.put(username, new UserData(userConf.getString(username)));
        }
        domain = config.getString("domain");
    }

    public boolean checkPassword(String username, String password) {
        return users.containsKey(username) && users.get(username).getPassword().equals(password);
    }

    public boolean existsUser(String username) {
        return users.containsKey(username);
    }

    public void addMailToRecepients(Mail mail) {
        // check username vs email
        for (String recipient : mail.getTo()) {
            String[] mailParts = recipient.split("@");
            if (mailParts[1].equals(domain) && existsUser(mailParts[0])) {
                users.get(mailParts[0]).addMail(mail);
            }
        }
    }

    public ConcurrentHashMap<Integer, Mail> getUserMails(String username) {
        if (!existsUser(username)) return null;

        return users.get(username).getMails();
    }

    public Mail deleteUserMail(String username, int id) {
        if (!existsUser(username)) return null;

        return users.get(username).deleteMail(id);
    }

    public Mail getUserMail(String username, int id) {
        if (!existsUser(username)) return null;

        return users.get(username).getMails().get(id);
    }
}
