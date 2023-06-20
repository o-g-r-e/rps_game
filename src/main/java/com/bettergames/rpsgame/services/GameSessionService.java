package com.bettergames.rpsgame.services;

import com.bettergames.rpsgame.data.entity.GameSession;
import com.bettergames.rpsgame.repository.GameSessionRepository;
import org.springframework.stereotype.Service;

@Service
public class GameSessionService {

    private final GameSessionRepository gameSessionRepository;

    public GameSessionService(GameSessionRepository gameSessionRepository) {
        this.gameSessionRepository = gameSessionRepository;
    }

    public GameSession getLastGameSessionByUserId(Integer userId) {
        return gameSessionRepository.getLastGameSessionByUserId(userId);
    }

    public GameSession getGameSessionByWsSessinoId(String wsSessionId) {
        return gameSessionRepository.findFirstByWsSessionId(wsSessionId);
    }

    public void saveGameSession(GameSession gameSession) {
        gameSessionRepository.save(gameSession);
    }
}
