package com.example.backend_chat.service;


import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserService {
    private final UserRepository userRepository ;
    @Autowired
    public UserService( UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    // createUser
    // check if user exist
    // encrypt password
    // save user info
    // send success respond
    public User createUser( User user) {
            User newUser = userRepository.findByEmail(user.getEmail());
            if (newUser != null) {
                userRepository.save(newUser);
                return newUser;
            }
            return null;
    }
    // login
    // check email and password
    // generate token
    // send success response with token

}
