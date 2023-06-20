package com.bettergames.rpsgame.game;

import java.util.*;

public class GameGlobals {

    public static Map<String, Set<String>> gameStageCommands;
    public static List<String> gameStages;
    public static List<String> guesses = List.of("rock", "paper", "scissors");

    public static GameResult gameResult(String userGuess, String computerGuess) {
        if(userGuess == null || "".equals(userGuess)) return GameResult.LOSE;
        if(computerGuess.equals(userGuess)) return GameResult.DRAW;

        if("rock".equals(userGuess) && "paper".equals(computerGuess) ||
           "paper".equals(userGuess) && "scissors".equals(computerGuess) ||
           "scissors".equals(userGuess) && "rock".equals(computerGuess)) {
            return GameResult.LOSE;
        }

        return GameResult.WIN;
    }

    static {
        gameStageCommands = new HashMap<>();
        Set<String> gameCommands = Set.of("logout", "rock", "paper", "scissors");
        gameStageCommands.put("menu", Set.of("start", "logout"));
        gameStageCommands.put("game1", gameCommands);
        gameStageCommands.put("game2", gameCommands);
        gameStageCommands.put("game3", gameCommands);
        gameStageCommands.put("gameover", Set.of("logout"));

        gameStages = List.of("menu", "game1", "game2", "game3", "gameover");
    }
}
