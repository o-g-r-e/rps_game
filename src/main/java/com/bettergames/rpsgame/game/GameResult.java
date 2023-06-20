package com.bettergames.rpsgame.game;

public enum GameResult {
    WIN("Win"), LOSE("Lose"), DRAW("Draw");

    private String stringResult;
    GameResult(String stringResult){
        this.stringResult = stringResult;
    }
    public String getResult(){ return stringResult;}
};
