package chadchat.domain.message;

public class MessageNotFoundException extends Exception {
    public MessageNotFoundException(int id) {
        super(String.valueOf(id));
    }
}
