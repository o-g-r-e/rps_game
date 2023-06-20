package com.bettergames.rpsgame.repository;

import com.bettergames.rpsgame.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    User findFirstByUsername(String username);
}
