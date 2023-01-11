package dslab.model;

import dslab.util.IntegrityUtil;

import java.util.Set;

public class Mail {
    private String from;
    private Set<String> to;
    private String subject;
    private String data;
    private String hash;
    private final IntegrityUtil integrityUtil = new IntegrityUtil();


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Set<String> getTo() {
        return to;
    }

    public void setTo(Set<String> to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setHash() {
        if (from == null || from.isBlank() || subject == null || subject.isBlank() || data == null || data.isBlank() || to == null || to.isEmpty()) return;
        hash = integrityUtil.calculateHash(toString());
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void reset() {
        from = subject = data = null;
        to = null;
    }

    public MailStatus getMailStatus() {
        if (from == null || from.isBlank()) {
            return MailStatus.MISSING_FROM;
        } else if (subject == null || subject.isBlank()) {
            return MailStatus.MISSING_SUBJECT;
        } else if (data == null || data.isBlank()) {
            return MailStatus.MISSING_DATA;
        } else if (to == null || to.isEmpty()) {
            return MailStatus.MISSING_TO;
        }
        return MailStatus.OK;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return getFrom() + "\n" + String.join(",", getTo()) + "\n" + getSubject() + "\n" + getData();
    }
}
