package com.bettergames.rpsgame.controller;

import com.bettergames.rpsgame.game.*;
import com.bettergames.rpsgame.data.entity.GameSession;
import com.bettergames.rpsgame.data.entity.User;
import com.bettergames.rpsgame.data.message.ResponseMessage;
import com.bettergames.rpsgame.data.message.UserMessage;
import com.bettergames.rpsgame.services.GameSessionService;
import com.bettergames.rpsgame.services.UserService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class WSController {
    private final UserService userService;
    private final GameSessionService gameSessionService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private Map<String, ThinkingUser> thinkingUsers;

    public WSController(UserService userService, GameSessionService gameSessionService, SimpMessagingTemplate simpMessagingTemplate) {
        this.userService = userService;
        this.gameSessionService = gameSessionService;
        this.simpMessagingTemplate = simpMessagingTemplate;
        thinkingUsers = new ConcurrentHashMap<>();

        scheduler.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                Iterator<Map.Entry<String, ThinkingUser>> iterator = thinkingUsers.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, ThinkingUser> entry = iterator.next();
                    ThinkingUser thinkingUser = entry.getValue();
                    long time = ((System.currentTimeMillis() / 1000) - thinkingUser.getStartTime())+thinkingUser.getTimePassed();

                    if(time >= thinkingUser.getTotalTime()) {
                        thinkingUser.sendTimeExpired();
                        iterator.remove();
                        continue;
                    }

                    long leftTime = thinkingUser.getTotalTime() - time;

                    if(leftTime > 14 && leftTime < 16) {
                        thinkingUser.sendTimeLeft(15);
                    } else if(leftTime > 9 && leftTime < 11) {
                        thinkingUser.sendTimeLeft(10);
                    } else if(leftTime > 4 && leftTime < 6) {
                        thinkingUser.sendTimeLeft(5);
                    } else if(leftTime > 2 && leftTime < 4) {
                        thinkingUser.sendTimeLeft(3);
                    } else if(leftTime > 0 && leftTime < 2) {
                        thinkingUser.sendTimeLeft(1);
                    }
                }
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @MessageMapping("/signup")
    public void signup(SimpMessageHeaderAccessor sha, UserMessage message) throws Exception {
        String[] messageData = message.getData().split(" ");
        String username = messageData[0];
        String password = messageData[1];

        if(userService.getUserByName(username) != null) {
            send(sha.getUser().getName(), message.getCommand(), "error", "user already exists");
            return;
        }

        userService.saveUser(new User(username, password, LocalDate.now()));

        send(sha.getUser().getName(), message.getCommand(), "success", "success registration of \""+username+"\"");
    }

    private GameSession createGameSession(User user, String wsSessionId) {
        GameSession userGameSession = new GameSession();
        userGameSession.setUser(user);
        userGameSession.setLastGameStage(GameStage.MENU.getStringValue());
        userGameSession.setWsSessionId(wsSessionId);
        userGameSession.setGameStageTime(System.currentTimeMillis() / 1000);
        return userGameSession;
    }

    @MessageMapping("/signin")
    public void signin(SimpMessageHeaderAccessor sha, UserMessage message) throws Exception {
        String[] messageData = message.getData().split(" ");
        String username = messageData[0];
        String password = messageData[1];

        User user = userService.getUserByName(username);

        if(user == null || !user.getPassword().equals(password)) {
            send(sha.getUser().getName(), message.getCommand(), "error", "user does not exist or invalid password");
            return;
        }

        user.setLastLoginTime(LocalDateTime.now());
        userService.saveUser(user);

        GameSession userGameSession = gameSessionService.getLastGameSessionByUserId(user.getId());

        if(userGameSession == null) {
            userGameSession = createGameSession(user, sha.getSessionId());
            gameSessionService.saveGameSession(userGameSession);
        }

        String greetingsMessage = "Hello "+username+"\n"+stageMessage(userGameSession.getLastGameStage());

        GameStage userStage = GameStage.getByStringValue(userGameSession.getLastGameStage());

        if(userStage == null) {
            send(sha.getUser().getName(), message.getCommand(), "error", "internal server error");
            return;
        }

        if(userStage.getStageTime() > 0) {
            thinkingUsers.put(sha.getUser().getName(), new ThinkingUser(sha.getUser().getName(), userGameSession.getGameStageTimePassed().intValue(), userStage.getStageTime(), simpMessagingTemplate));
            greetingsMessage += "\nTime left: "+(userStage.getStageTime() - userGameSession.getGameStageTimePassed());
        }

        ResponseMessage responseMessage = new ResponseMessage(message.getCommand(), "success", greetingsMessage);
        responseMessage.setUserId(user.getId());

        send(sha.getUser().getName(), responseMessage);
    }

    private String stageMessage(String gameStage) {
        return "Now you in "+gameStage+" stage, you can run next commands:\n"
                + String.join(", ", GameGlobals.gameStageCommands.get(gameStage));
    }

    @MessageMapping("/startGame")
    public void startGame(SimpMessageHeaderAccessor sha, UserMessage message) throws Exception {
        User user = userService.getUserById(message.getUserId());

        if(user == null) {
            send(sha.getUser().getName(), message.getCommand(), "error", "user not found");
            return;
        }

        GameSession userGameSession = gameSessionService.getLastGameSessionByUserId(user.getId());

        if(userGameSession == null) {
            userGameSession = createGameSession(user, sha.getSessionId());
        } else {
            GameStage userStage = GameStage.getByStringValue(userGameSession.getLastGameStage());
            if(userStage != GameStage.MENU) {
                send(sha.getUser().getName(), message.getCommand(), "error", "invalid command");
                return;
            }
        }

        userGameSession.setLastGameStage(GameStage.GAME1.getStringValue());
        GameStage userStage = GameStage.getByStringValue(userGameSession.getLastGameStage());
        thinkingUsers.put(sha.getUser().getName(), new ThinkingUser(sha.getUser().getName(), userGameSession.getGameStageTimePassed().intValue(), userStage.getStageTime(), simpMessagingTemplate));
        gameSessionService.saveGameSession(userGameSession);

        send(sha.getUser().getName(), message.getCommand(), "success", "Game started.\n"+stageMessage(userGameSession.getLastGameStage())+"\nTime left: "+userStage.getStageTime()+" seconds");
    }

    @MessageMapping("/userGuess/{guess}")
    public void userGuess(@DestinationVariable String guess, SimpMessageHeaderAccessor sha, UserMessage message) throws Exception {
        User user = userService.getUserById(message.getUserId());

        if(user == null) {
            send(sha.getUser().getName(), message.getCommand(), "error", "user not found");
            return;
        }

        GameSession userGameSession = gameSessionService.getLastGameSessionByUserId(user.getId());

        GameStage userStage = GameStage.getByStringValue(userGameSession.getLastGameStage());

        if(userGameSession == null || userStage == GameStage.GAMEOVER || userStage == GameStage.MENU || !GameGlobals.guesses.contains(guess)) {
            send(sha.getUser().getName(), message.getCommand(), "error", "invalid command");
            return;
        }

        String computerGuess = GameGlobals.guesses.get((int)(Math.random()*3));
        userGameSession.setGameStageResult(userGameSession.getLastGameStage(), guess + "/" +computerGuess);

        userGameSession.setNextGameStage();
        userStage = GameStage.getByStringValue(userGameSession.getLastGameStage());
        if(userStage.getStageTime() > 0) {
            thinkingUsers.put(sha.getUser().getName(), new ThinkingUser(sha.getUser().getName(), userGameSession.getGameStageTimePassed().intValue(), userStage.getStageTime(), simpMessagingTemplate));
        }

        if(userStage == GameStage.GAMEOVER) {
            userGameSession.setSessionResult(userGameSession.sessionResult().getResult());
        }

        gameSessionService.saveGameSession(userGameSession);

        send(sha.getUser().getName(), message.getCommand(), "success", buildResponseMessage(guess, computerGuess, userGameSession.getSessionResult(), userStage));
    }

    private ChangeStageCheckStatus checkChangeStageConditions(User user, GameSession userGameSession) {
        if(user == null) {
            return new ChangeStageCheckStatus("user not found");
        }

        if(userGameSession == null) {
            return new ChangeStageCheckStatus("game session not found");
        }

        GameStage userStage = GameStage.getByStringValue(userGameSession.getLastGameStage());

        if(userStage.getStageTime() <= 0) {
            return new ChangeStageCheckStatus("invalid stage");
        }

        if((System.currentTimeMillis() / 1000) - userGameSession.getGameStageTime() < userStage.getStageTime()) {
            return new ChangeStageCheckStatus("time not expired");
        }

        return new ChangeStageCheckStatus();
    }

    @MessageMapping("/nextStage")
    public void nextStage(SimpMessageHeaderAccessor sha, UserMessage message) throws Exception {
        User user = userService.getUserById(message.getUserId());

        GameSession userGameSession = gameSessionService.getLastGameSessionByUserId(user.getId());

        ChangeStageCheckStatus changeStageCheckStatus = checkChangeStageConditions(user, userGameSession);

        if(!changeStageCheckStatus.isSuccess()) {
            send(sha.getUser().getName(), new ResponseMessage("change stage", "failed", changeStageCheckStatus.getErrorMessage()));
            return;
        }

        GameStage userStage = GameStage.getByStringValue(userGameSession.getLastGameStage());

        if(userStage == GameStage.GAMEOVER) {
            userGameSession.setCompleteStatus(1);
        } else {
            userGameSession.setGameStageResult(userGameSession.getLastGameStage(), "");
        }

        userGameSession.setNextGameStage();
        userStage = GameStage.getByStringValue(userGameSession.getLastGameStage());

        if(userStage == GameStage.GAMEOVER) {
            userGameSession.setSessionResult(userGameSession.sessionResult().getResult());
        }

        gameSessionService.saveGameSession(userGameSession);

        if(userStage.getStageTime() > 0) {
            thinkingUsers.put(sha.getUser().getName(), new ThinkingUser(sha.getUser().getName(), userGameSession.getGameStageTimePassed().intValue(), userStage.getStageTime(), simpMessagingTemplate));
        }

        send(sha.getUser().getName(), "change stage", "success", buildResponseMessage("", "",  userGameSession.getSessionResult(), userStage));
    }

    private String buildResponseMessage(String userGuess, String computerGuess, String sessionResult, GameStage gameStage) {
        String result = "";

        if(gameStage == GameStage.GAME2 || gameStage == GameStage.GAME3 || gameStage == GameStage.GAMEOVER) {
            result += "\nYour guess: "+userGuess+"\nComputer guess: "+computerGuess+"\nGame result: "+GameGlobals.gameResult(userGuess, computerGuess).getResult();
        }

        if(sessionResult != null) {
            result += "\nSession result: "+sessionResult;
        }

        result += "\n"+stageMessage(gameStage.getStringValue());

        if(gameStage.getStageTime() > 0) {
            result += "\nTime left: "+gameStage.getStageTime()+" seconds";
        }

        return result;
    }

    @MessageMapping("/logout")
    public void logout(SimpMessageHeaderAccessor sha, UserMessage message) throws Exception {
        User user = userService.getUserById(message.getUserId());

        if(user == null) {
            send(sha.getUser().getName(), message.getCommand(), "error", "user not found");
            return;
        }

        GameSession userGameSession = gameSessionService.getLastGameSessionByUserId(user.getId());

        GameStage userStage = GameStage.getByStringValue(userGameSession.getLastGameStage());

        if(userGameSession != null && userStage.getStageTime() > 0) {

            userGameSession.setGameStageTimePassed((System.currentTimeMillis() / 1000) - userGameSession.getGameStageTime());

            if(userStage == GameStage.GAMEOVER) {
                userGameSession.setNextGameStage();
            }

            gameSessionService.saveGameSession(userGameSession);
        }

        send(sha.getUser().getName(), message.getCommand(), "suscess", "success logout");
    }

    private void send(String userName, String command, String status, String message) {
        ResponseMessage responseMessage = new ResponseMessage(command, status, message);
        simpMessagingTemplate.convertAndSendToUser(userName, "/queue/messages", responseMessage);
    }

    private void send(String userName, ResponseMessage responseMessage) {
        simpMessagingTemplate.convertAndSendToUser(userName, "/queue/messages", responseMessage);
    }
}
