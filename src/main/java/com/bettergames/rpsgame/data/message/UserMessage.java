package com.bettergames.rpsgame.data.message;

public class UserMessage {

    private Integer userId;
    private String command;
    private String data;

    public UserMessage() {}

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "UserMessage{" +
                "userId=" + userId +
                ", command='" + command + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}