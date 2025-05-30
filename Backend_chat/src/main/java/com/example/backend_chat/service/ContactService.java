package com.example.backend_chat.service;

import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService{
    private UserRepository userRepository;
    @Autowired
    public ContactService( UserRepository userRepository) {
        this.userRepository = userRepository;
    }

}
