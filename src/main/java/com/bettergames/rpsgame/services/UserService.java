package com.bettergames.rpsgame.services;

import com.bettergames.rpsgame.data.entity.User;
import com.bettergames.rpsgame.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public User getUserByName(String username) {
        return userRepository.findFirstByUsername(username);
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id).get();
    }
}
