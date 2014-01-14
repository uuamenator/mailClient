package mailclient.ui.console;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Scanner;
import mailclient.core.EmailAddress;
import mailclient.core.EmailClient;
import mailclient.core.EmailMessage;

public final class Main {

    private final static Scanner in = new Scanner(System.in);
    private final static PrintStream out = System.out;
    private static boolean messagesIsAfterSearch = false;

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
        
        out.println("Mail Client Version 1.00");
        out.print("Your email: ");
        String userEmail = readEmailAddress();
//        String userEmail = "uuamenator@gmail.com";
        out.print("Your password: ");
//        out.println();
        String password = readPassword();
//        String password = "";
        EmailClient emailClient = new EmailClient(userEmail, password);
//        password = null; // padidinti shansus kad garbage collector nutrins passworda
        ArrayList<EmailMessage> messages = listMessages(emailClient, null, null);
        int lastDisplayedMessageIndex = -1;
        while (true) {
            try {
//                out.print("Enter message number to read, 'l' to list, 'w' to write a new message, 'q' to quit: ");
                String menuReplyOption = "";
                if (lastDisplayedMessageIndex >= 0) 
                    menuReplyOption = "'r' to reply, ";
                String command = readString("Enter " + menuReplyOption + "message number to read, 'sort' to select sorting, 's' to search, 'l' to list, 'w' to write a new message, 'q' to quit");
//                in.nextLine().trim();
                if (command.equalsIgnoreCase("q")) 
                    return;
                else if (command.equalsIgnoreCase("l")) 
                    messages = listMessages(emailClient, null, null);
                else if (command.equalsIgnoreCase("w")) 
                    send(emailClient, null);
                else if (command.equalsIgnoreCase("sort"))
                    setSortBy(emailClient);
                else if (command.equalsIgnoreCase("up")) {
                    emailClient.scrollListing(true);
                    messages = listMessages(emailClient, null, null);
                } else if (command.equalsIgnoreCase("down")) {
                    emailClient.scrollListing(false);
                    messages = listMessages(emailClient, null, null);
                } else if (command.equalsIgnoreCase("s")) {
                    String searchFor = readString("Enter phrase to search for");
                    String searchType = readString("Enter where to search: from 'f' to 't' subject 's' or press enter to search body");
                    if (searchType.equalsIgnoreCase("f") || searchType.equalsIgnoreCase("t") || searchType.equalsIgnoreCase("s"))
                        messages = listMessages(emailClient, searchFor, searchType);
                    else
                        messages = listMessages(emailClient, searchFor, null);
                } else if (command.equalsIgnoreCase("r") && lastDisplayedMessageIndex >= 0) 
                    send(emailClient, messages.get(lastDisplayedMessageIndex));
                else if (command.matches("^[0-9]+$")) {
                    int messageToDisplay;
                    if (messagesIsAfterSearch)
                        messageToDisplay = Integer.parseInt(command) - 1;
                    else
                        messageToDisplay = Integer.parseInt(command) - emailClient.getListingIndexFrom();
                    displayMessage(messages.get(messageToDisplay), emailClient);
                    lastDisplayedMessageIndex = messageToDisplay;
                } else 
                    out.println("Invalid command");
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

    private static void displayMessage(EmailMessage message, EmailClient emailClient) throws Exception {
        SimpleDateFormat dateformatJava = new SimpleDateFormat("dd-MM-yyyy");
        String messageDate = dateformatJava.format(message.getDate());
        out.println("-----------------------------------------------------------------------");
        out.println("Date:    " + messageDate);
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
        emailClient.markMessageAsRead(message.getMessageNumberInFolder());

    }

    private static String readString(String query) {
        out.print(query + ": ");
        return in.nextLine().trim();
    }
    
    private static ArrayList<EmailMessage> listMessages(EmailClient emailClient, String searchFor, String searchType) throws Exception {
        out.print("Listing mail...");
        ArrayList<EmailMessage> messages;
        if (searchFor == null)
            messages = emailClient.listMessages(null, null);
        else 
            messages = emailClient.listMessages(searchFor, searchType);
        out.println();
        if (messages.isEmpty()) {
            out.println("You have no mail.");
            return messages;
        } else
            emailClient.emailSort(messages);
        out.println("#   Date  From                                        Subject");
        out.println("--- ----- ------------------------------------------- --------------------------");
        for (int i = 0; i < messages.size(); i++) {
            EmailMessage message = messages.get(i);
            String subject = message.getSubject();
            if (subject == null) {
                subject = "(no subject)";
            }
//            subject = subject.substring(0, 50);               sdelat' if string > x...
            SimpleDateFormat dateformatJava = new SimpleDateFormat("dd-MM-yyyy");
            String messageDate = dateformatJava.format(message.getDate());
            messageDate = messageDate.substring(0,5);
            if (searchFor == null){
                if (message.isSeen()) 
                    out.printf("%-3d %-5s %-43s %s\n", i + emailClient.getListingIndexFrom(), messageDate, message.getFrom(), subject);
                else {
                    out.print("\033[1m");
                    out.printf("%-3d %-5s %-43s %s\n", i + emailClient.getListingIndexFrom(), messageDate, message.getFrom(), subject);
                    out.print("\033[0m");
                }
            } else {
                if (message.isSeen()) 
                    out.printf("%-3d %-5s %-43s %s\n", i + 1, messageDate, message.getFrom(), subject);
                else {
                    out.print("\033[1m");
                    out.printf("%-3d %-5s %-43s %s\n", i + 1, messageDate, message.getFrom(), subject);
                    out.print("\033[0m");
                }
                
            }
            
        }
        out.println("--------------------------------------------------------------------------");
        if (searchFor == null) {
            messagesIsAfterSearch = false;
            out.println("Listed messages " + emailClient.getListingIndexFrom() + " to " +
                    emailClient.getListingIndexTo() + " from total " +
                    emailClient.getMessageCountInFolder() + " messages in Inbox");
            if (emailClient.getMessageCountInFolder() > 20)
                out.println("Enter 'up' or 'down' to list more mail");
            out.println("--------------------------------------------------------------------------");
        } else
            messagesIsAfterSearch = true;
        return messages;
    }

    
    private static void setSortBy(EmailClient emailClient) {
        out.println("Sorting available by date (d), from (f), Subject (s); can descend (-) or ascend (+).");
        String sortByField = readString("Enter d, f or s");
        String sortingOrder = readString("Enter + or -");
        if ("d".equalsIgnoreCase(sortByField) && "+".equals(sortingOrder))
            emailClient.setSortBy(EmailClient.SortBy.DATE_ASC);
        else if ("d".equalsIgnoreCase(sortByField) && "-".equals(sortingOrder))
            emailClient.setSortBy(EmailClient.SortBy.DATE_DESC);
        else if ("f".equalsIgnoreCase(sortByField) && "+".equals(sortingOrder))
            emailClient.setSortBy(EmailClient.SortBy.FROM_ASC);
        else if ("f".equalsIgnoreCase(sortByField) && "-".equals(sortingOrder))
            emailClient.setSortBy(EmailClient.SortBy.FROM_DESC);
        else if ("s".equalsIgnoreCase(sortByField) && "+".equals(sortingOrder))
            emailClient.setSortBy(EmailClient.SortBy.SUBJECT_ASC);
        else if ("s".equalsIgnoreCase(sortByField) && "-".equals(sortingOrder))
            emailClient.setSortBy(EmailClient.SortBy.SUBJECT_DESC);
        else
            out.println("Wrong selection. Please select from d+, d-, f+, f-, s+ and s-");
    }
    
}
