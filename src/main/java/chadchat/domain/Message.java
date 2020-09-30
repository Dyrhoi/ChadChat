package chadchat.domain;

import chadchat.domain.user.User;

import java.time.LocalDateTime;

public class Message {
    private String message;
    private LocalDateTime time;
    private User user;

    public Message(String message, User user) {
        this.message = message;
        this.time = LocalDateTime.now();
        this.user = user;
    }

    public String formatTime(){
        String weekDay = time.getDayOfWeek().name();
        String timeClock = time.getHour() + ":"+ time.getMinute();
        return weekDay + " " + timeClock;
    }

    @Override
    public String toString() {
        return formatTime() + "\n" + user.getUsername() +" : "+ message;
    }
}
