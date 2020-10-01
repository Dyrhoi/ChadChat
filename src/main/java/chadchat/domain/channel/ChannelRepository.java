package chadchat.domain.channel;

public interface ChannelRepository {
    Iterable<Channel> findAllChannels();
}
