package chadchat.domain;

public class UserExistsException extends Exception {

        public UserExistsException(String name) {
            super("User already exists: " + name);
        }
}
