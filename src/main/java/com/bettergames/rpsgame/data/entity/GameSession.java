package com.bettergames.rpsgame.data.entity;

import com.bettergames.rpsgame.game.GameGlobals;
import com.bettergames.rpsgame.game.GameResult;
import com.bettergames.rpsgame.game.GameStage;
import jakarta.persistence.*;

@Entity
public class GameSession {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String lastGameStage;
    private Long gameStageTime;
    @Column(nullable = false)
    private Long gameStageTimePassed = 0L;
    private String game1Result;
    private String game2Result;
    private String game3Result;
    private String sessionResult;
    @Column(nullable = false)
    private Integer completeStatus = 0;
    @Column(nullable = false)
    private String wsSessionId;

    public GameSession() {}

    public String getWsSessionId() {
        return wsSessionId;
    }

    public void setWsSessionId(String wsSessionId) {
        this.wsSessionId = wsSessionId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getLastGameStage() {
        return lastGameStage;
    }

    public void setLastGameStage(String lastGameStage) {
        this.lastGameStage = lastGameStage;
    }

    public Long getGameStageTime() {
        return gameStageTime;
    }

    public void setGameStageTime(Long gameStageTime) {
        this.gameStageTime = gameStageTime;
    }

    public String getGame1Result() {
        return game1Result;
    }

    public void setGame1Result(String game1Result) {
        this.game1Result = game1Result;
    }

    public String getGame2Result() {
        return game2Result;
    }

    public void setGame2Result(String game2Result) {
        this.game2Result = game2Result;
    }

    public String getGame3Result() {
        return game3Result;
    }

    public void setGame3Result(String game3Result) {
        this.game3Result = game3Result;
    }

    public Long getGameStageTimePassed() {
        return gameStageTimePassed;
    }

    public void setGameStageTimePassed(Long gameStageTimePassed) {
        this.gameStageTimePassed = gameStageTimePassed;
    }

    public String getSessionResult() {
        return sessionResult;
    }

    public void setSessionResult(String sessionResult) {
        this.sessionResult = sessionResult;
    }

    public Integer getCompleteStatus() {
        return completeStatus;
    }

    public void setCompleteStatus(Integer completeStatus) {
        this.completeStatus = completeStatus;
    }

    public boolean isComplete() {
        return getCompleteStatus() == 1;
    }

    public void setNextGameStage() {
        GameStage nextStage = GameStage.getNextStage(GameStage.getByStringValue(this.getLastGameStage()));
        this.setLastGameStage(nextStage.getStringValue());
        this.setGameStageTime(System.currentTimeMillis() / 1000);
        this.setGameStageTimePassed(0L);
    }

    public void setGameStageResult(String gameStage, String value) {
        switch (gameStage) {
            case "game1":
                this.setGame1Result(value);
                break;
            case "game2":
                this.setGame2Result(value);
                break;
            case "game3":
                this.setGame3Result(value);
                break;
        }
    }

    public GameResult sessionResult() {
        int wins = 0;
        String[] game1Guesses = getGame1Result().split("/");

        GameResult game1Result = game1Guesses.length<=1?GameResult.LOSE:GameGlobals.gameResult(game1Guesses[0], game1Guesses[1]);

        if(game1Result == GameResult.WIN) wins++;

        String[] game2Guesses = getGame2Result().split("/");
        GameResult game2Result = game2Guesses.length<=1?GameResult.LOSE:GameGlobals.gameResult(game2Guesses[0], game2Guesses[1]);

        if(game2Result == GameResult.WIN) wins++;

        String[] game3Guesses = getGame3Result().split("/");
        GameResult game3Result = game3Guesses.length<=1?GameResult.LOSE:GameGlobals.gameResult(game3Guesses[0], game3Guesses[1]);

        if(game3Result == GameResult.WIN) wins++;

        if(game1Result == GameResult.DRAW && game2Result == GameResult.DRAW && game3Result == GameResult.DRAW) return GameResult.DRAW;

        if(wins >= 2) return GameResult.WIN;

        return GameResult.LOSE;
    }
}
