package chadchat.API;

import chadchat.domain.message.Message;
import chadchat.domain.message.MessageNotFoundException;
import chadchat.domain.message.MessageRepository;
import chadchat.domain.user.User;
import chadchat.domain.user.UserExistsException;
import chadchat.domain.user.UserNotFoundException;
import chadchat.domain.user.UserRepository;

public class ChadChat {
    private final UserRepository users;
    private final MessageRepository messages;
    public ChadChat(UserRepository users, MessageRepository messages) {
        this.users = users;
        this.messages = messages;
    }

    public User createUser(String username, String password) throws UserExistsException {
        byte[] salt = User.generateSalt();
        byte[] secret = User.calculateSecret(salt, password);
        return users.createUser(username, salt, secret);
    }

    public User findUser(String username) throws UserNotFoundException {
        return users.findUser(username);
    }

    public User login(String name, String password) throws InvalidPasswordException, UserNotFoundException {
        User user = users.findUser(name);
        if (user.isPasswordCorrect(password)) {
            return user;
        } else  {
            throw new InvalidPasswordException();
        }
    }

    public Message createMessage(String message, User user) {
        try {
            return messages.createMessage(user.getId(), message);
        } catch (MessageNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }


}
