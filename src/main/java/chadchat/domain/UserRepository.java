package chadchat.domain;

public interface UserRepository  {
    
        User findUser(String name);
        Iterable<User> findAllUsers();

    User createUser(String name, byte[] salt, byte[] secret) throws UserExistsException;
}

