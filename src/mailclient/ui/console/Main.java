package mailclient.ui.console;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import mailclient.core.EmailAddress;
import mailclient.core.EmailClient;
import mailclient.core.EmailMessage;

public final class Main {

    private final static Scanner in = new Scanner(System.in);
    private final static PrintStream out = System.out;

    public static void main(String[] args) throws Exception {

//        String boldGrayLine = "\033[1mThis is a BOLD line\033[0m";
//        String setBold = "\033[1mThis keeps it bold.";
//        String normalLine = "This is a normal (default) line";
//        String setNormal = "\033[0mThis un-bolds it.";
//        System.out.println(normalLine);
//        System.out.println(boldGrayLine);
//        System.out.println(normalLine);
//        System.out.println(setBold);
//        System.out.println(normalLine);
//        System.out.println(setNormal);
//        System.out.println(normalLine);
        
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
        ArrayList<EmailMessage> messages = listMessages(emailClient, null);
        int lastDisplayedMessageIndex = -1;
        while (true) {
            try {
//                out.print("Enter message number to read, 'l' to list, 'w' to write a new message, 'q' to quit: ");
                String menuReplyOption = "";
                if (lastDisplayedMessageIndex >= 0) {
                    menuReplyOption = "'r' to reply, ";
                }
                String command = readString("Enter " + menuReplyOption + "message number to read, 's' to search, 'l' to list, 'w' to write a new message, 'q' to quit");
//                in.nextLine().trim();
                if (command.equalsIgnoreCase("q")) {
                    return;
                } else if (command.equalsIgnoreCase("l")) {
                    messages = listMessages(emailClient, null);
                } else if (command.equalsIgnoreCase("w")) {
                    send(emailClient, null);
                } else if (command.equalsIgnoreCase("s")) {
                    String searchFor = readString("Enter phrase to search for");
                    messages = listMessages(emailClient, searchFor);
                } else if (command.equalsIgnoreCase("r") && lastDisplayedMessageIndex >= 0) {
                    send(emailClient, messages.get(lastDisplayedMessageIndex));
                } else if (command.matches("^[0-9]+$")) {
                    int messageToDisplay = Integer.parseInt(command) - 1;
                    displayMessage(messages.get(messageToDisplay));
                    lastDisplayedMessageIndex = messageToDisplay;
                } else {
                    out.println("Invalid command");
                }
            } catch (Exception ex) {
                if (ex.getMessage() == null) {
                    out.println(ex.getClass().getCanonicalName());
                } else {
                    out.println("Error: " + ex.getMessage());
                }
            }
        }
//        displayMessage(messages.get(0));
//        send(emailClient);
    }

    private static String readEmailAddress() {
        while (true) {
            String address = in.nextLine().trim();
            if (EmailAddress.isValidEmailAddress(address)) {
                return address;
            } else {
                out.print("Invalid email address, try again: ");
            }
        }
    }

    private static String readPassword() {
        if (System.console() != null) {
            return String.valueOf(System.console().readPassword());
        } else {
            return in.nextLine();
        }
    }

    private static void send(EmailClient emailClient, EmailMessage messageToReplyTo) throws Exception {
        String to;
        String subject;
        if (messageToReplyTo == null) {
            to = readString("To");
            subject = readString("Subject");

        } else {
            to = messageToReplyTo.getFrom();
            subject = "Re: " + messageToReplyTo.getSubject();
            out.printf("To: %s\nSubject: %s\n", to, subject);
        }

        out.println("Message text, terminated by a line containing a single dot:");
        String text = readText();
        if (messageToReplyTo != null) {
            text = text + "\n\n\n> " + messageToReplyTo.getText().trim().replace("\n", "\n> ");


        }
        ArrayList<String> filePaths = new ArrayList<String>();
        String attachmentInput;
        while ((attachmentInput = readString("Enter filepath to add file, or enter to continue)")).length() > 0) {
            filePaths.add(attachmentInput.trim());
        }
        out.print("Sending message...");
        emailClient.sendMessage(new EmailMessage(emailClient.getUserEmail(), to, subject, text, false), filePaths);
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

//    private static ArrayList<EmailMessage> listMessages(EmailClient emailClient) throws Exception {
//        ArrayList<EmailMessage> messages = emailClient.listMessages();
//        if (messages.isEmpty()) {
//            out.println("You have no mail.");
//            return messages;
//        }
//        int howManyM = emailClient.MessageCountNumber();
//        out.print("\n You have total " + howManyM + " messages. \n");
//        int first = howManyM - 20;
//        if (first < 0) {
//            first = 0;
//        }
//        if (howManyM > 20) {
//            out.print("\n Now will be shown not more than 20 earliest messages.\n");
//            out.print("\n Do you want to change that number? (y/n)\n");
//            String yesNO = in.nextLine();
//            if (yesNO.equalsIgnoreCase("y")) {
//                out.print("\n Type the number of messages you want to see. \n");
//                String firstString = in.nextLine();
//                try {
//                    if (firstString != null) {
//                        first = Integer.parseInt(firstString);
//                        if (howManyM < first) {
//                            first = howManyM;
//                        }
//                    }
//                    out.println("#   From                                     Subject");
//                    out.println("----- ---------------------------------------- --------------------------");
//                    for (int i = howManyM - first; i < messages.size(); i++) {
//                        EmailMessage message = messages.get(i);
//                        String subject = message.getSubject();
//                        if (subject == null) {
//                            subject = "(no subject)";
//                        }
//                        out.printf("%-5d %-40s %s\n", i + 1, message.getFrom(), subject);
//                    }
//                    out.println("-----------------------------------------------------------------------");
//                } catch (NumberFormatException e) {
//                    out.print("\n Your input isn't a nummber! \n");
//                    listMessages(emailClient);
//                }
//            } else if (yesNO.equalsIgnoreCase("n")) {
//                try {
//                    out.println("#   From                                     Subject");
//                    out.println("----- ---------------------------------------- --------------------------");
//                    for (int i = first; i < messages.size(); i++) {
//                        EmailMessage message = messages.get(i);
//                        String subject = message.getSubject();
//                        if (subject == null) {
//                            subject = "(no subject)";
//                        }
//                        out.printf("%-5d %-40s %s\n", i + 1, message.getFrom(), subject);
//                    }
//                    out.println("-----------------------------------------------------------------------");
//                } catch (NumberFormatException e) {
//                    out.print("\n Your input isn't a nummber! \n");
//                    listMessages(emailClient);
//                }
//            } else {
//                out.print("\n Your input is wrong! \n");
//                listMessages(emailClient);
//            }
//            return messages;
//        } else {
//            out.println();
//            if (messages.isEmpty()) {
//                out.println("You have no mail.");
//                return messages;
//            }
//            out.println("#   From                                     Subject");
//            out.println("----- ---------------------------------------- --------------------------");
//            for (int i = 0; i < messages.size(); i++) {
//                EmailMessage message = messages.get(i);
//                String subject = message.getSubject();
//                if (subject == null) {
//                    subject = "(no subject)";
//                }
//                out.printf("%-5d %-40s %s\n", i + 1, message.getFrom(), subject);
//            }
//            out.println("-----------------------------------------------------------------------");
//            return messages;
//        }
//    }

    private static void displayMessage(EmailMessage message) {
        out.println("-----------------------------------------------------------------------");
        out.println("From:    " + message.getFrom());
        out.println("To:      " + message.getTo());
        String subject = message.getSubject();
        if (subject == null)
            subject = "(no subject)";
        out.println("Subject: " + subject);
        out.println();
        if (message.getText() == null){
            out.println("no text");
        } else
            out.println(message.getText().trim());
        out.println("-----------------------------------------------------------------------");

    }

    private static String readString(String query) {
        out.print(query + ": ");
        return in.nextLine().trim();
    }
    
    private static ArrayList<EmailMessage> listMessages(EmailClient emailClient, String searchFor) throws Exception {
        out.print("Listing mail...");
        ArrayList<EmailMessage> messages;
        if (searchFor == null)
            messages = emailClient.listMessages(null);
        else 
            messages = emailClient.listMessages(searchFor);
        out.println();
        if (messages.isEmpty()) {
            out.println("You have no mail.");
            return messages;
        }
        out.println("#   From                                     Subject");
        out.println("--- ----- ---------------------------------------- --------------------------");
        for (int i = 0; i < messages.size(); i++) {
            EmailMessage message = messages.get(i);
            String subject = message.getSubject();
            if (subject == null) {
                subject = "(no subject)";
            }
//            subject = subject.substring(0, 50);               sdelat' if string > x...
            if (message.isSeen()) 
                out.printf("%-3d %-40s %s\n", i + 1, message.getFrom(), subject);
            else {
                out.print("\033[1m");
                SimpleDateFormat dateformatJava = new SimpleDateFormat("dd-MM-yyyy");
                String messageDate = dateformatJava.format(message.getData());
                messageDate = messageDate.substring(0,5);
                out.printf("%-3d %-5s %-40s %s\n", i + 1, messageDate, message.getFrom(), subject);
                out.print("\033[0m");
            }
                
            
        }
        out.println("-----------------------------------------------------------------------");
        return messages;
    }

    
}
