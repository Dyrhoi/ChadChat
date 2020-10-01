package chadchat.domain.user;

public interface UserRepository {

    User findUser(String name) throws UserNotFoundException;

    Iterable<User> findAllUsers();

    Iterable<User> findAllUsersByChannel(int id);

    User createUser(String name, byte[] salt, byte[] secret) throws UserExistsException;
}

