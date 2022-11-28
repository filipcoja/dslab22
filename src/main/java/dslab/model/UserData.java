package dslab.model;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class UserData {
    private final String password;
    private final ConcurrentHashMap<Integer, Mail> mails = new ConcurrentHashMap<>();
    private int id;

    public UserData(String password) {
        this.password = password;
        this.id = 0;
    }

    public String getPassword() {
        return password;
    }

    public synchronized void addMail(Mail mail) {
        id++;
        mails.put(id, mail);
    }

    public Mail deleteMail(int id) {
        return mails.remove(id);
    }

    public ConcurrentHashMap<Integer, Mail> getMails() {
        return mails;
    }
}
