package mailclient.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailClient {

    private final String userEmail;
    private final Session session;
    private final Folder inbox;

    
    public EmailClient(final String userEmail, final String password) throws Exception {
        this.userEmail = userEmail;
        
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.imap.host", "imap.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.store.protocol", "imaps");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.imap.port", "993");
        props.put("mail.smtp.port", "465");
        session = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userEmail, password);
            }
        });
        Store store = session.getStore("imaps");
        store.connect("imap.gmail.com", userEmail, password); // userEmail password are ignored
        inbox = store.getFolder("Inbox");
        inbox.open(Folder.READ_ONLY);
    }

    public ArrayList<EmailMessage> listMessages() throws Exception {
        Message messages[] = inbox.getMessages();
        ArrayList<EmailMessage> result = new ArrayList<EmailMessage>();
        for(Message message:messages) {
            result.add(new EmailMessage(
                    addressesToString(message.getFrom()),
                    addressesToString(message.getAllRecipients()),
                    message.getSubject(),
                    getContentText(message.getContent())));
        }
        return result;
    }
    
    public String getUserEmail() {
        return userEmail;
    }

    public void sendMessage(EmailMessage message, ArrayList<String> filePaths) throws Exception {
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(message.getFrom()));
        mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(message.getTo()));
        mimeMessage.setSubject(message.getSubject());
        mimeMessage.setText(message.getText());
        if (!filePaths.isEmpty()) {
            MimeMultipart mimeMultipart = new MimeMultipart();
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(message.getText());
            mimeMultipart.addBodyPart(textPart);
            
            for (int i = 0; i < filePaths.size(); i++) {
                MimeBodyPart mimeBodyPart = new MimeBodyPart();
                DataSource source = new FileDataSource(filePaths.get(i));
                mimeBodyPart.setDataHandler(new DataHandler(source));
                File file = new File(filePaths.get(i));
                mimeBodyPart.setFileName(file.getName());
                mimeMultipart.addBodyPart(mimeBodyPart);
            }
            mimeMessage.setContent(mimeMultipart);
        }
        Transport.send(mimeMessage);        
    }

    
    private String addressesToString(Address[] addresses) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < addresses.length; i++) {
            if (i != 0)
                buffer.append(", ");
            buffer.append(((InternetAddress) addresses[i]).getAddress());
        }
        return buffer.toString();
    }

    private String getContentText(Object contentObject) throws Exception {
        if (contentObject instanceof String)
            return contentObject.toString();
        if (contentObject instanceof Multipart) {
            BodyPart clearTextPart = null;
            BodyPart htmlTextPart = null;
            Multipart content = (Multipart) contentObject;
            for (int i = 0; i < content.getCount(); i++) {
                BodyPart part = content.getBodyPart(i);
                if (part.isMimeType("text/plain")) {
                    clearTextPart = part;
                    break;
                } else if (part.isMimeType("text/html")) {
                    htmlTextPart = part;
                }
            }

            if (clearTextPart != null) {
                return clearTextPart.getContent().toString();
            } else if (htmlTextPart != null) {
                String html = htmlTextPart.getContent().toString();
                return html;
            }
        }
        return "Error, this email doesn't contain text.";
    }
    
}
