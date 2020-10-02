package chadchat.domain.channel;

import chadchat.domain.message.Message;
import chadchat.domain.message.MessageNotFoundException;
import chadchat.domain.user.User;
import chadchat.domain.user.UserExistsException;

import java.util.List;

public interface ChannelRepository {
    Iterable<Channel> findAllChannelsByUser(int userId);
    Iterable<Channel> findAllChannels();
    Channel findChannel(int id);
    Channel createChannel(String name, int userid) throws ChannelNotFoundException;
    Channel joinChannel(int userId, int channelId) throws UserExistsException;
    boolean leaveChannel(int userId, int channelId);
    Channel findChannel(String name) throws ChannelNotFoundException;
    Iterable<User> findUsersByChannel(int channelId) throws ChannelNotFoundException;

}
