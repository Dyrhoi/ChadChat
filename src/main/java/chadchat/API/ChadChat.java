package chadchat.API;

import chadchat.domain.User;
import chadchat.domain.UserRepository;

public class ChadChat {
    private final UserRepository users;
    public ChadChat(UserRepository users) {
        this.users = users;
    }

    public User createUser(String name, String password)  {
        byte[] salt = User.generateSalt();
        byte[] secret = User.calculateSecret(salt, password);
        return users.createUser(name, salt, secret);
    }
}
