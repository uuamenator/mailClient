package mailclient.core;

import java.util.Date;
import javax.mail.Message;

public class EmailMessage {
    private final String from;
    private final String to;
    private final String subject;
    private final String text;
    private final boolean seen;
    private int messageNumberInFolder = 0;
    private Date data;

    public EmailMessage(String from, String to, String subject, String text, boolean seen, int messageNumberInFolder, Date data) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.text = text;
        this.seen = seen;
        this.messageNumberInFolder = messageNumberInFolder;
        this.data = data;
    }

    public EmailMessage(String from, String to, String subject, String text, boolean seen) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.text = text;
        this.seen = seen;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }
    
    public boolean isSeen() {
        return seen;
    }
    
    
    public int getMessageNumberInFolder () {
        return messageNumberInFolder;
    }
    
    //add field = message number
    //add field = message received date
    
    public Date getData() {
        return data;
    }
    
}
