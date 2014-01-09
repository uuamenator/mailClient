package mailclient.ui.console;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;
import mailclient.core.EmailAddress;
import mailclient.core.EmailClient;
import mailclient.core.EmailMessage;

public final class Main {

    private final static Scanner in = new Scanner(System.in);
    private final static PrintStream out = System.out;
            
    public static void main(String[] args) throws Exception {
        out.println("Mail Client Version 0.01");
        out.print("Your email: ");
        String userEmail = readEmailAddress();
//        String userEmail = "uuamenator@gmail.com";
        out.print("Your password: ");
//        out.println();
        String password = readPassword();
//        String password = "";
        EmailClient emailClient = new EmailClient(userEmail, password);
//        password = null; // padidinti shansus kad garbage collector nutrins passworda
        ArrayList<EmailMessage> messages = listMessages(emailClient);
        while (true) {
            try {
//                out.print("Enter message number to read, 'l' to list, 'w' to write a new message, 'q' to quit: ");
                String command = readString("Enter message number to read, 'L' to list, 'W' to write a new message, 'Q' to quit");
//                in.nextLine().trim();
                if (command.equalsIgnoreCase("q"))
                    return;
                else if (command.equalsIgnoreCase("l"))
                    messages = listMessages(emailClient);
                else if (command.equalsIgnoreCase("w"))
                    send(emailClient);
                else if (command.matches("^[0-9]+$"))
                    displayMessage(messages.get(Integer.parseInt(command) - 1));
                else
                    out.println("Invalid command");
            } 
            catch (Exception ex) {
                if (ex.getMessage() == null)
                    out.println(ex.getClass().getCanonicalName());
                else
                    out.println("Error: " + ex.getMessage());
            }
        }
//        displayMessage(messages.get(0));
//        send(emailClient);
    }
    
    private static String readEmailAddress() {
        while (true) {
            String address = in.nextLine().trim();
            if (EmailAddress.isValidEmailAddress(address))
                return address;
            else
                out.print("Invalid email address, try again: ");
        }
    }
    
    private static String readPassword() {
        if (System.console() != null)
            return String.valueOf(System.console().readPassword());
        else
            return in.nextLine();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    private static void send(EmailClient emailClient) throws Exception {
//        out.print("To: ");
//        String to = in.nextLine();
        String to = readString("To");
//        out.print("Subject: ");
//        String subject = in.nextLine();
        String subject = readString("Subject");
        out.println("Message text, terminated by a line containing a single dot:");
        String text = readText();
        ArrayList<String> filePaths = new ArrayList<String>();
//        ArrayList<String> fileNames = null;
//        String attachmentInput = readString("Enter full filepath to attach a file (example c:\\Program Files\\file.txt)\nor \".\" to skip");
//        if (!".".equals(attachmentInput)) {
//            filePaths = new ArrayList<String>();
//            fileNames = new ArrayList<String>();
        String attachmentInput;
        while ((attachmentInput = readString("Enter filepath to add file, or enter to continue)")).length() > 0) 
            filePaths.add(attachmentInput.trim());
        out.print("Sending message...");
        emailClient.sendMessage(new EmailMessage(emailClient.getUserEmail(), to, subject, text), filePaths);
        out.println(" Message sent.");
    }
    
    private static String readText() {
        StringBuilder buffer = new StringBuilder();
        String line = in.nextLine();
        while (!line.equals(".")) {
            buffer.append(line);
            buffer.append('\n');
            line = in.nextLine();
        }
        return buffer.toString();
    }
    
    private static ArrayList<EmailMessage> listMessages(EmailClient emailClient) throws Exception {
        out.print("Listing mail...");
        ArrayList<EmailMessage> messages = emailClient.listMessages();
        out.println();

        if (messages.isEmpty()) {
            out.println("You have no mail.");
            return messages;
        }
        out.println("#   From                                     Subject");
        out.println("--- ---------------------------------------- --------------------------");
        for (int i = 0; i < messages.size(); i++) {
            EmailMessage message = messages.get(i);
            String subject = message.getSubject();
            if (subject == null)
                subject = "(no subject)";
//            subject = subject.substring(0, 50);               sdelat' if string > x...
            out.printf("%-3d %-40s %s\n", i + 1, message.getFrom(), subject);
        }
        out.println("-----------------------------------------------------------------------");
        return messages;
    }
    
    private static void displayMessage(EmailMessage message) {
        out.println("-----------------------------------------------------------------------");
        out.println("From:    " + message.getFrom());
        out.println("To:      " + message.getTo());
        out.println("Subject: " + message.getSubject());
        out.println();
        out.println(message.getText().trim());
        out.println("-----------------------------------------------------------------------");
        
    }

    private static String readString(String query) {
        out.print(query + ": ");
        return in.nextLine().trim();
    }
    
    public static void printLine(String string) {
        out.println(string);
    }
}
