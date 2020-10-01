package chadchat.domain.message;

import chadchat.domain.user.User;

import java.time.LocalDateTime;

public class Message {
    private final String message;
    private final LocalDateTime time;
    private final User user;
    private final int id;

    public Message(int id, String message, User user) {
        this.id = id;
        this.message = message;
        this.time = LocalDateTime.now();
        this.user = user;
    }

    public String formatTime(){
        String weekDay = time.getDayOfWeek().name();
        String timeClock = String.format("%02d:%02d", time.getHour(), time.getMinute());
        return weekDay + " " + timeClock;
    }

    @Override
    public String toString() {
        return formatTime() + "\n" + user.getUsername() +" : "+ message;
    }
}