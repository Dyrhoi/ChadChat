package chadchat.domain.message;

import chadchat.domain.user.User;
import chadchat.domain.user.UserExistsException;
import chadchat.domain.user.UserNotFoundException;

public interface MessageRepository {

    Message findMessage(int id) throws MessageNotFoundException;

    Iterable<Message> findAllMessagesByUser(int userid);

    Iterable<Message> findAllMessages();

    Message createMessage(int userid, String message) throws MessageNotFoundException;

}
