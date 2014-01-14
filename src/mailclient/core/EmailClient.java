package mailclient.core;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.BodyTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.RecipientStringTerm;
import javax.mail.search.SubjectTerm;

public class EmailClient {


    public enum SortBy {DATE_ASC, DATE_DESC, FROM_ASC, FROM_DESC, SUBJECT_ASC, SUBJECT_DESC}
    private final String userEmail;
    private final Session session;
    private final Folder inbox;
    private       SortBy sortBy = SortBy.DATE_ASC;
    private       int messageCountInFolder;
    private int listingIndexFrom; // when listMail is called, mail is extracted in interval between listingIndexFrom 
    private int listingIndexTo;   // and listingIndexTo from Folder;
    

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
        this.resetListingIndexes();
    }

    public ArrayList<EmailMessage> listMessages(String searchKey, String searchType) throws Exception {
        Message messages[];
        BodyTerm bodyTerm;
        RecipientStringTerm recipientStringTerm;
        FromStringTerm fromStringTerm;
        SubjectTerm subjectTerm;
        if (searchKey == null){
            if (messageCountInFolderChanged())
                resetListingIndexes();
            messages = inbox.getMessages(listingIndexFrom, listingIndexTo);
        } else {
            if (searchType == null) {
                bodyTerm = new BodyTerm(searchKey.trim());
                messages = inbox.search(bodyTerm);
            } else if ("f".equalsIgnoreCase(searchType)) {
                fromStringTerm = new FromStringTerm(searchKey.trim());
                messages = inbox.search(fromStringTerm);
            } else if ("t".equalsIgnoreCase(searchType)) {
                recipientStringTerm = new RecipientStringTerm(Message.RecipientType.TO, searchKey.trim());
                messages = inbox.search(recipientStringTerm);
            } else if ("s".equalsIgnoreCase(searchType)) {
                subjectTerm = new SubjectTerm(searchKey.trim());
                messages = inbox.search(subjectTerm);
            } else {
                bodyTerm = new BodyTerm(searchKey.trim());
                messages = inbox.search(bodyTerm);
            }
        }
        return convertMessageToEmailMessage(messages);
    }

    private void resetListingIndexes() throws Exception {
        listingIndexTo = messageCountInFolder = inbox.getMessageCount();
        if (messageCountInFolder > 20) {
            listingIndexFrom = listingIndexTo - 19;
        } else
            listingIndexFrom = 1;
    }
    
    public void scrollListing(boolean directionIsUp) throws Exception {
        if (messageCountInFolderChanged()) {
            resetListingIndexes();
        } else if (messageCountInFolder > 20) {
            if (directionIsUp) {
                if (listingIndexFrom - 20 >= 1) {
                    listingIndexFrom -= 20;
                    listingIndexTo -= 20;
                } else {
                    listingIndexTo -= listingIndexFrom - 1;
                    listingIndexFrom = 1;
                }
                    
            } else {
                if (listingIndexTo +20 <= messageCountInFolder) {
                    listingIndexTo += 20;
                    listingIndexFrom += 20;
                } else {
                    listingIndexFrom += messageCountInFolder - listingIndexTo;
                    listingIndexTo = messageCountInFolder;
                }
                    
            }
            
        } else
            resetListingIndexes();
    }

    private boolean messageCountInFolderChanged() throws Exception {
        int newMessageCountInFolder = inbox.getMessageCount();
        return messageCountInFolder != newMessageCountInFolder;
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
                String filePath = filePaths.get(i);
                DataSource source = new FileDataSource(filePath);
                mimeBodyPart.setDataHandler(new DataHandler(source));
                mimeBodyPart.setFileName(new File(filePath).getName());
                mimeMultipart.addBodyPart(mimeBodyPart);
            }
            mimeMessage.setContent(mimeMultipart);
        }
        Transport.send(mimeMessage);
    }

    private String addressesToString(Address[] addresses) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < addresses.length; i++) {
            if (i != 0) {
                buffer.append(", ");
            }
            buffer.append(((InternetAddress) addresses[i]).getAddress());
        }
        return buffer.toString();
    }


    public int MessageCountNumber() {
        try {
            int Number = inbox.getMessageCount();
            return Number;
        } catch (MessagingException e) {
            return -1;
        }
    }
    
//    public ArrayList<EmailMessage> searchMessagesFor(String searchKey) throws Exception {
//        BodyTerm bodyTerm = new BodyTerm(searchKey);
////        
////    AddressStringTerm - This abstract class implements string comparisons for Message addresses.
////    BodyTerm - This class implements searches on a message body.
////    FromStringTerm - This class implements string comparisons for the From Address header.
////    HeaderTerm - This class implements comparisons for Message headers.
////    MessageIDTerm - This term models the RFC822 "MessageId" - a message-id for Internet messages that is supposed to be unique per message.
////    RecipientStringTerm - This class implements string comparisons for the Recipient Address headers.
////    SubjectTerm - This class implements comparisons for the message Subject header.
//      
//        Message[] messages = inbox.search(bodyTerm);
//        return convertMessageToEmailMessage(messages);
//    }

    private ArrayList<EmailMessage> convertMessageToEmailMessage(Message[] messages) throws Exception{
        ArrayList<EmailMessage> result = new ArrayList<EmailMessage>();
        for (Message message : messages) {
            result.add(new EmailMessage(
                    addressesToString(message.getFrom()),
                    addressesToString(message.getAllRecipients()),
                    message.getSubject(),
//                    getContentText(message.getContent()),
                    getMessageText(message),
                    message.isSet(Flags.Flag.SEEN),
                    message.getMessageNumber(),
                    message.getSentDate(),
                    messageHasAttachments(message)
                    ));
        }
        return result;
    }


    private String getMessageText(Message message) throws Exception {
        Object content = message.getContent();
        String result = null;
        if (content instanceof String) {
            result = (String) content;
        } else if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            result = getMultipartText(multipart);
        }
        return result;
    }

    private String getMultipartText(Multipart multipart) throws Exception {
        BodyPart clearTextPart = null;
        BodyPart htmlTextPart = null;
        String result = null;
        Message message;
        int count = multipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodypart = multipart.getBodyPart(i);
            if (bodypart.isMimeType("text/plain")) {
                clearTextPart = bodypart;
                break;
            } else if (bodypart.isMimeType("text/html")) {
                htmlTextPart = bodypart;
            }
            
            Object content = bodypart.getContent();
            
            if (content instanceof String)
                result = (String) content;
            else if (content instanceof InputStream)
                continue;
            else if (content instanceof Multipart)
                result = getMultipartText ((Multipart) content);
            else if (content instanceof Message) {
                message = (Message) content;
                result = getMessageText(message);
            }
        }
        
        if (clearTextPart != null) {
            return clearTextPart.getContent().toString();
        } else if (htmlTextPart != null) {
            String html = htmlTextPart.getContent().toString();
            return html;
        } else
            return result;
    }

//    private String getContentText(Object contentObject) throws Exception {
//        if (contentObject instanceof String) {
//            return contentObject.toString();
//        }
//        if (contentObject instanceof Multipart) {
//            BodyPart clearTextPart = null;
//            BodyPart htmlTextPart = null;
//            Multipart content = (Multipart) contentObject;
//            for (int i = 0; i < content.getCount(); i++) {
//                BodyPart part = content.getBodyPart(i);
//                if (part.isMimeType("text/plain")) {
//                    clearTextPart = part;
//                    break;
//                } else if (part.isMimeType("text/html")) {
//                    htmlTextPart = part;
//                }
//            }
//
//            if (clearTextPart != null) {
//                return clearTextPart.getContent().toString();
//            } else if (htmlTextPart != null) {
//                String html = htmlTextPart.getContent().toString();
//                return html;
//            }
//        }
//        return null;
//    }
    
    
    public void markMessageAsRead(int messageIndexInFolder) throws Exception {
        inbox.close(false);
        inbox.open(Folder.READ_WRITE);
        Message msg = inbox.getMessage(messageIndexInFolder);
        Object content = msg.getContent();
        if (content instanceof Multipart) 
            readAndDiscardMultipart((Multipart)content);
//        MimeMessage source = (MimeMessage) inbox.getMessage(messageIndexInFolder);
//        MimeMessage copy = new MimeMessage(source);
//        inbox.getMessage(messageIndexInFolder).getContent();
        inbox.close(false);
        inbox.open(Folder.READ_ONLY);
    }

    private void readAndDiscardMultipart(Multipart multipart) throws Exception{
        for (int i=0; i<multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            Object content = bodyPart.getContent();
            if (content instanceof Multipart) {
                readAndDiscardMultipart((Multipart)content);
            }
        }
    }
    
    public void setSortBy(SortBy sortBy) {
        this.sortBy = sortBy;
    }

    public void emailSort(ArrayList<EmailMessage> messages) {
        if (sortBy == SortBy.FROM_ASC) 
            Collections.sort(messages, new Comparator<EmailMessage>() {
                @Override
                public int compare(EmailMessage  message1, EmailMessage  message2)
                {
                    return  message1.getFrom().compareTo(message2.getFrom());
                }
            });
        else if (sortBy == SortBy.FROM_DESC)
            Collections.sort(messages, new Comparator<EmailMessage>() {
                @Override
                public int compare(EmailMessage  message1, EmailMessage  message2)
                {
                    return  message2.getFrom().compareTo(message1.getFrom());
                }
            });
        else if (sortBy == SortBy.DATE_DESC)
            Collections.sort(messages, new Comparator<EmailMessage>() {
                @Override
                public int compare(EmailMessage  message1, EmailMessage  message2)
                {
                    return  message2.getDate().compareTo(message1.getDate());
                }
            });
        else if (sortBy == SortBy.SUBJECT_ASC)
            Collections.sort(messages, new Comparator<EmailMessage>() {
                @Override
                public int compare(EmailMessage  message1, EmailMessage  message2)
                {
                    if (message1.getSubject() == null && message2.getSubject() == null)
                        return " ".compareTo(" ");
                    else if (message1.getSubject() == null && message2.getSubject() != null)
                        return " ".compareTo(message2.getSubject());
                    else if (message1.getSubject() != null && message2.getSubject() == null)
                        return message1.getSubject().compareTo(" ");
                    return  message1.getSubject().compareTo(message2.getSubject());
                }
            });
        else if (sortBy == SortBy.SUBJECT_DESC)
            Collections.sort(messages, new Comparator<EmailMessage>() {
                @Override
                public int compare(EmailMessage  message1, EmailMessage  message2)
                {
                    if (message2.getSubject() == null && message1.getSubject() == null)
                        return " ".compareTo(" ");
                    else if (message2.getSubject() == null && message1.getSubject() != null)
                        return " ".compareTo(message1.getSubject());
                    else if (message2.getSubject() != null && message1.getSubject() == null)
                        return message2.getSubject().compareTo(" ");
                    return  message2.getSubject().compareTo(message1.getSubject());
                }
            });
        
    }

    public int getMessageCountInFolder() {
        return messageCountInFolder;
    }
    
    public int getListingIndexFrom() {
        return listingIndexFrom;
    }
    
    public int getListingIndexTo() {
        return listingIndexTo;
    }
        
    
    public static boolean messageHasAttachments(Message message) throws Exception {
        Object content = message.getContent();
        boolean result = false;
        if (content instanceof String) {
            result = false;
        } else if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            result = multipartHasAttachments(multipart);
        }
        return result;
    } 
    
    private static boolean multipartHasAttachments(Multipart multipart) throws Exception{
        boolean result = false;
        Message message;
                
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            Object content = bodyPart.getContent();
            if (content instanceof Multipart) {
                if (multipartHasAttachments((Multipart)content))
                    return true;
            } else if (content instanceof Message) {
                message = (Message) content;
                if (messageHasAttachments(message))
                    return true;
            } else if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                   !"".equals(bodyPart.getFileName())) {
                return true; // dealing with attachments only
                
            } 
        }            
        
        return result;
    }
    
    public ArrayList<String> getAttachmentFilenamesFromMessageIndex(int messageIndex) throws Exception {
        ArrayList<String> fileNamesSizes;
        Multipart multipart = (Multipart) inbox.getMessage(messageIndex).getContent();
        fileNamesSizes = getAttachmentFilenamesFromMultipart(multipart);
        return fileNamesSizes;
    }
    
    private ArrayList<String> getAttachmentFilenamesFromMultipart(Multipart multipart) throws Exception {    
        ArrayList<String> fileNamesSizes = new ArrayList<String>();
//        ArrayList<String> fileNameSizesBranch = new ArrayList<String>();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
//            if (bodyPart.getContent() instanceof Multipart)
//                 fileNameSizesBranch = getAttachmentFilenamesFromMultipart((Multipart) bodyPart.getContent());
//            if (bodyPart.getContent() instanceof InputStream) {
                if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                           "".equals(bodyPart.getFileName()))
                    continue; // dealing with attachments only
                fileNamesSizes.add(bodyPart.getFileName());
                fileNamesSizes.add(String.valueOf(bodyPart.getSize()));
//            }
//            for (int tempArrayIndex = 0; tempArrayIndex < fileNameSizesBranch.size(); tempArrayIndex += 2){
//                fileNamesSizes.add(fileNameSizesBranch.get(tempArrayIndex));
//                fileNamesSizes.add(fileNameSizesBranch.get(tempArrayIndex+1));
//            }
        }
        return fileNamesSizes;
    }

    public ArrayList<InputStream> getInputStreamsFromMessageIndex(int messageIndex) throws Exception {
        ArrayList<InputStream> fileInputStreams;
        Multipart multipart = (Multipart) inbox.getMessage(messageIndex).getContent();
        fileInputStreams = getInputStreamsFromMultipart(multipart);
        return fileInputStreams;
    }
        
    private ArrayList<InputStream> getInputStreamsFromMultipart(Multipart multipart) throws Exception {
        ArrayList<InputStream> fileInputStreams = new ArrayList<InputStream>();
//        ArrayList<InputStream> fileInputStreamsBranch = new ArrayList<InputStream>();
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
//            if (bodyPart.getContent() instanceof Multipart)
//                 fileInputStreamsBranch = getInputStreamsFromMultipart((Multipart) bodyPart.getContent());
//            if (bodyPart.getContent() instanceof InputStream) {
                if(!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition()) &&
                           "".equals(bodyPart.getFileName())) 
                    continue; // dealing with attachments only
                fileInputStreams.add(bodyPart.getInputStream());
//            }
//            for (int tempArrayIndex = 0; tempArrayIndex < fileInputStreamsBranch.size(); tempArrayIndex += 2)
//                 fileInputStreams.add(fileInputStreamsBranch.get(tempArrayIndex));
        }
        return fileInputStreams;
    }

    public void saveFile(String folder, int messageNumberInFolder, int toSave) throws Exception {
        ArrayList<InputStream> fileInputStreams = getInputStreamsFromMessageIndex(messageNumberInFolder);
        ArrayList<String> fileNamesSizes = getAttachmentFilenamesFromMessageIndex(messageNumberInFolder);
        if (toSave != 0) {
            InputStream is = fileInputStreams.get(toSave);
            File f = new File(fileNamesSizes.get(toSave * 2));
            FileOutputStream fos = new FileOutputStream(f);
            byte[] buf = new byte[4096];
            int bytesRead;
            while((bytesRead = is.read(buf))!=-1) {
                fos.write(buf, 0, bytesRead);
            }
            fos.close();
        } else if (toSave == 0) {
            for (int i = 0; i < fileInputStreams.size(); i++) {
                InputStream is = fileInputStreams.get(i);
                File f = new File(fileNamesSizes.get(i * 2));
                FileOutputStream fos = new FileOutputStream(f);
                byte[] buf = new byte[4096];
                int bytesRead;
                while((bytesRead = is.read(buf))!=-1) {
                    fos.write(buf, 0, bytesRead);
                }
                fos.close();
            }
        }
    }

//            InputStream is = bodyPart.getInputStream();
//            File f = new File("/tmp/" + bodyPart.getFileName());
//            FileOutputStream fos = new FileOutputStream(f);
//            byte[] buf = new byte[4096];
//            int bytesRead;
//            while((bytesRead = is.read(buf))!=-1) {
//                fos.write(buf, 0, bytesRead);
//            }
//            fos.close();
//            attachments.add(f);
    
}



