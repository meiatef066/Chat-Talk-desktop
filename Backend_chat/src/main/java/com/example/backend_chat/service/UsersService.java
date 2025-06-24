package com.example.backend_chat.service;

import com.example.backend_chat.DTO.ContactResponse;
import com.example.backend_chat.DTO.SimpleUserDTO;
import com.example.backend_chat.model.Contact;
import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.ContactRepository;
import com.example.backend_chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UsersService {
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
@Autowired
    public UsersService( UserRepository userRepository, ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }
    public List<ContactResponse> searchUser_v2(String query, String currentUserEmail) {
        if (query == null || query.trim().isEmpty()) return List.of();

        List<User> users = userRepository.searchUsersByQuery(query.trim());
        List<User> filteredUsers = users.stream()
                .filter(user -> !user.getEmail().equals(currentUserEmail))
                .toList();

        List<String> targetEmails = filteredUsers.stream()
                .map(User::getEmail)
                .toList();

        // 🔥 Fetch all contacts in 1 query
        List<Contact> contacts = contactRepository
                .findAllContactsBetweenUserAndTargetUsers(currentUserEmail, targetEmails);

        // Map contact pairs to status
        Map<String, String> contactStatusMap = new HashMap<>();
        for (Contact contact : contacts) {
            String email = contact.getUser().getEmail().equals(currentUserEmail)
                    ? contact.getContact().getEmail()
                    : contact.getUser().getEmail();
            contactStatusMap.put(email, contact.getStatus().name());
        }

        return filteredUsers.stream()
                .map(user -> {
                    String status = contactStatusMap.getOrDefault(user.getEmail(), "NONE");

                    return ContactResponse.builder()
                            .id(user.getId())
                            .contact(SimpleUserDTO.builder()
                                    .id(user.getId())
                                    .email(user.getEmail())
                                    .firstName(user.getFirstName() != null ? user.getFirstName() : "")
                                    .lastName(user.getLastName() != null ? user.getLastName() : "")
                                    .profilePicture(user.getProfilePicture())
                                    .build())
                            .status(status)
                            .build();
                })
                .collect(Collectors.toList());
    }

}

