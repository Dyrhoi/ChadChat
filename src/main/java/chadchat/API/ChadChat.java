package chadchat.API;

import chadchat.domain.channel.Channel;
import chadchat.domain.channel.ChannelNotFoundException;
import chadchat.domain.channel.ChannelRepository;
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
    private final ChannelRepository channels;

    public ChadChat(UserRepository users, MessageRepository messages, ChannelRepository channels) {
        this.users = users;
        this.messages = messages;
        this.channels = channels;
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
        } else {
            throw new InvalidPasswordException();
        }
    }

    public Message createMessage(String message, User user) {
        try {
            return messages.createMessage(user.getId(), message, user.getCurrentChannel());
        } catch (MessageNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Iterable<Channel> findAllChannels() {
        return channels.findAllChannels();
    }

    public Channel createChannel(String name, User user) throws ChannelNotFoundException, RuntimeException {
        return channels.createChannel(name, user.getId());
    }

    public Channel joinChannel(String name, User user) throws ChannelNotFoundException, UserExistsException {
        int id = user.getId();
        Channel channel = null;

        channel = channels.findChannel(name);
        channels.joinChannel(id, channel.getId());

        return channel;
    }

    public boolean channelContainsUser(User user, int channelId) throws ChannelNotFoundException {
        for (User u : channels.findUsersByChannel(channelId)) {
            if (u.equals(user))
                return true;
        }
        return false;
    }

    public Channel leaveChannel(String name, User user) throws ChannelNotFoundException, UserExistsException {
        int id = user.getId();
        Channel channel = null;

        channel = channels.findChannel(name);
        channels.leaveChannel(id, channel.getId());

        return channel;
    }

    public boolean findUsersByChannel(String name, User user) throws ChannelNotFoundException {
        int channelId = channels.findChannel(name).getId();
        return channelContainsUser(user, channelId);
    }
}