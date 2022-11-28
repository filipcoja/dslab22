package dslab.model;

import java.util.Set;

public class Mail {
    private String from;
    private Set<String> to;
    private String subject;
    private String data;

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

    @Override
    public String toString() {
        return "Mail{" +
                "from='" + from + '\'' +
                ", to=" + to +
                ", subject='" + subject + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
