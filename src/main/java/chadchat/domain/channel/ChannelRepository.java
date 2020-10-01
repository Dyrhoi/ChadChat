package chadchat.domain.channel;

import chadchat.domain.message.Message;
import chadchat.domain.message.MessageNotFoundException;

public interface ChannelRepository {
    Iterable<Channel> findAllChannels();
    Channel findChannel(int id);
    Channel createChannel(String name, int userid) throws ChannelNotFoundException;
}
