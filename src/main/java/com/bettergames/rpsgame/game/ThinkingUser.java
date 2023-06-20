package com.bettergames.rpsgame.game;

import com.bettergames.rpsgame.data.message.ResponseMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Objects;

public class ThinkingUser {

    private String userName;
    private long startTime;
    private int timePassed;
    private int totalTime;
    private SimpMessagingTemplate simpMessagingTemplate;

    public ThinkingUser(String userName, int timePassed, int totalTime, SimpMessagingTemplate simpMessagingTemplate) {
        this.userName = userName;
        this.startTime = System.currentTimeMillis() / 1000;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.timePassed = timePassed;
        this.totalTime = totalTime;
    }

    public String getUserName() {
        return userName;
    }

    public Long getStartTime() {
        return startTime;
    }

    public SimpMessagingTemplate getSimpMessagingTemplate() {
        return simpMessagingTemplate;
    }

    public void sendTimeLeft(long timeLeft) {
        this.getSimpMessagingTemplate().convertAndSendToUser(this.getUserName(), "/queue/messages", new ResponseMessage("", "timeLeft", "Time left: "+timeLeft));
    }

    public void sendTimeExpired() {
        this.getSimpMessagingTemplate().convertAndSendToUser(this.getUserName(), "/queue/messages", new ResponseMessage("", "timeExpired", "Time expired."));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThinkingUser that = (ThinkingUser) o;
        return userName.equals(that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName);
    }

    public int getTimePassed() {
        return timePassed;
    }

    public int getTotalTime() {
        return totalTime;
    }
}
