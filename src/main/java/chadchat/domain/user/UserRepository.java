package chadchat.domain.user;

public interface UserRepository {

    User findUser(String name) throws UserNotFoundException;

    Iterable<User> findAllUsers();

    User createUser(String name, byte[] salt, byte[] secret) throws UserExistsException;
}

