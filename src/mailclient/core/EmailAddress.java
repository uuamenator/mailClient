package mailclient.core;

import java.util.regex.Pattern;

public class EmailAddress {
    
    private static final Pattern VALID_EMAIL_ADDRESS = Pattern.compile(
        "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

    
//    private final String address;
//
//    public EmailAddress(String address) {
//        if (!isValidEmailAddress(address))
//            throw new IllegalArgumentException("Invalid email address " + address);
//        this.address = address;
//    }
//
//    @Override
//    public String toString() {
//        return address;
//    }
    
    public static boolean isValidEmailAddress(String address) {
        return VALID_EMAIL_ADDRESS.matcher(address).matches();
    }
    
    
}
