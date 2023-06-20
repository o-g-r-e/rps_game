package com.bettergames.rpsgame.configuration;


import com.bettergames.rpsgame.data.entity.GameSession;
import com.bettergames.rpsgame.game.GameStage;
import com.bettergames.rpsgame.services.GameSessionService;
import com.bettergames.rpsgame.services.UserService;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketInterceptor implements ChannelInterceptor {

    private final UserService userService;
    private final GameSessionService gameSessionService;

    public WebSocketInterceptor(UserService userService, GameSessionService gameSessionService) {
        this.userService = userService;
        this.gameSessionService = gameSessionService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        return ChannelInterceptor.super.preSend(message, channel);
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {

        StompHeaderAccessor stompDetails = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (stompDetails != null) {

            if (stompDetails.getCommand() == null) return;

            String sessionId = stompDetails.getSessionId();

            switch (stompDetails.getCommand()) {
                case CONNECT:
                    System.out.println("STOMP Connect [sessionId: " + sessionId + "]");
                    break;
                case CONNECTED:
                    System.out.println("STOMP Connected [sessionId: " + sessionId + "]");
                    break;
                case DISCONNECT:
                    System.out.println("STOMP Disconnect [sessionId: " + sessionId + "]");

                    GameSession gameSession = gameSessionService.getGameSessionByWsSessinoId(sessionId);

                    GameStage userStage = GameStage.getByStringValue(gameSession.getLastGameStage());

                    if(gameSession != null && userStage.getStageTime() > 0) {

                        gameSession.setGameStageTimePassed((System.currentTimeMillis() / 1000) - gameSession.getGameStageTime());

                        gameSessionService.saveGameSession(gameSession);
                    }
                    break;
                default:
                    break;

            }
        }
    }
}
