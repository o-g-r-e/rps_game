package com.bettergames.rpsgame.repository;

import com.bettergames.rpsgame.data.entity.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GameSessionRepository extends JpaRepository<GameSession, Integer> {

    @Query(value = "SELECT * FROM game_session WHERE user_id=?1 AND complete_status != 1 ORDER BY id ASC LIMIT 1", nativeQuery = true)
    GameSession getLastGameSessionByUserId(Integer userId);

    GameSession findFirstByWsSessionId(String wsSessionId);
}
