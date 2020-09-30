package chadchat.domain.user;

public class UserNotFoundException extends Exception {

    public UserNotFoundException(String name) {
        super(name);
    }
}
