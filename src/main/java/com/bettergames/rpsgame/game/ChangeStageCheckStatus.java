package com.bettergames.rpsgame.game;

public class ChangeStageCheckStatus {
    private boolean success;
    private String errorMessage;

    public ChangeStageCheckStatus(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }

    public ChangeStageCheckStatus() {
        this.success = true;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
