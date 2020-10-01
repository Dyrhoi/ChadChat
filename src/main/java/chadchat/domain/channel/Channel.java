package chadchat.domain.channel;

import chadchat.domain.user.User;

import java.time.LocalDateTime;

public class Channel {
    private final String name;
    private final LocalDateTime time;
    private final int id;

    public Channel(int id, String name) {
        this.id = id;
        this.name = name;
        this.time = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }
}
