package com.example.backend_chat.service;

import com.example.backend_chat.DTO.LoginRequest;
import com.example.backend_chat.exception.UserNotFoundException;
import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * 📁 7. service/
 * 🔹 AuthService.java
 * Register:
 *
 * Validates email uniqueness
 *
 * Hashes password
 *
 * Saves user
 *
 * Returns JWT
 *
 * Login:
 *
 * Uses AuthenticationManager
 *
 * Validates user
 *
 * Returns JWT
 * */
@Service
public class AuthService {
    private final UserRepository userRepository;
    @Autowired
    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    // create account
    public User createUser( User user) {
        User newUser = userRepository.findByEmail(user.getEmail());
        if (newUser != null) {
            userRepository.save(newUser);
            return newUser;
        }
        return null;
    }
    // login
//    public User isloginUser( LoginRequest loginRequest ) {
//        User newUser = userRepository.findByEmail(loginRequest.getEmail());
//        if(newUser == null) {
//          return new  UserNotFoundException;
//        }
//    }
}
