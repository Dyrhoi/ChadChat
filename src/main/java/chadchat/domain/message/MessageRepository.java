package chadchat.domain.message;

import chadchat.domain.user.User;
import chadchat.domain.user.UserExistsException;
import chadchat.domain.user.UserNotFoundException;

public interface MessageRepository {

    Message findMessage(int id) throws MessageNotFoundException;

    Iterable<Message> findAllMessages();

    Iterable<Message> findAllMessagesByUser(int userId);

    Iterable<Message> findAllMessagesByChannelId(int channelId);

    Message createMessage(int userid, String message, int channelid) throws MessageNotFoundException;

}
