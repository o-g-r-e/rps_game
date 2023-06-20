package com.bettergames.rpsgame.game;

import java.util.List;

public enum GameStage {
    MENU("menu", 0), GAME1("game1", 30), GAME2("game2", 30), GAME3("game3", 30), GAMEOVER("gameover", 5);

    private String stringValue;
    private int stageTime;
    private List<String> commands;
    GameStage(String stringValue, int stageTime) {
        this.stringValue = stringValue;
        this.stageTime = stageTime;
    }

    public static GameStage getNextStage(GameStage stage) {
        if(stage.ordinal() == GameStage.values().length-1) return GameStage.values()[0];

        return GameStage.values()[stage.ordinal()+1];
    }

    public static GameStage getByStringValue(String stringValue) {
        for (int i = 0; i < GameStage.values().length; i++) {
            if(GameStage.values()[i].getStringValue().equals(stringValue)) return GameStage.values()[i];
        }
        return null;
    }

    public String getStringValue() {
        return stringValue;
    }

    public int getStageTime() {
        return stageTime;
    }
}
