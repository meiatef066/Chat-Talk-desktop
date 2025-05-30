package com.example.backend_chat.service;

import com.example.backend_chat.DTO.UserSearchDTO;
import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
@Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public List<UserSearchDTO> searchUser(String query) {
        List<User> users = userRepository.searchUsersByQuery(query);
        return users.stream()
                .map(user -> new UserSearchDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName() + " " + user.getLastName(),
                        user.getProfilePicture()
                ))
                .collect(Collectors.toList());
    }
}

