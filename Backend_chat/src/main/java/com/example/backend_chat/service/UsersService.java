package com.example.backend_chat.service;

import com.example.backend_chat.DTO.UserSearchDTO;
import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsersService {
    private final UserRepository userRepository;
@Autowired
    public UsersService( UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    public List<UserSearchDTO> searchUser(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        List<User> users = userRepository.searchUsersByQuery(query.trim());
        return users.stream()
                .map(user -> new UserSearchDTO(
                        user.getId(),
                        user.getEmail(),
                        (user.getFirstName() != null ? user.getFirstName() : "") + " " +
                                (user.getLastName() != null ? user.getLastName() : ""),
                        user.getProfilePicture()
                ))
                .toList();
    }
}

