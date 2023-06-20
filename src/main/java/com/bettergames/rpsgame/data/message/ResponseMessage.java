package com.bettergames.rpsgame.data.message;

public class ResponseMessage {

    private String command;
    private String status;
    private String message;

    private Integer userId;

    public ResponseMessage() {
    }

    public ResponseMessage(String command, String status, String message) {
        this.command = command;
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}