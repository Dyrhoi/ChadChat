package chadchat.domain.user;

public class UserExistsException extends Exception {

        public UserExistsException(String name) {
            super("User already exists: " + name);
        }
}
