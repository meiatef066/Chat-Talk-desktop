package com.example.backend_chat.service;


import com.example.backend_chat.DTO.UpdateUserProfileRequest;
import com.example.backend_chat.exception.UserNotFoundException;
import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
    private final UserRepository userRepository ;
    @Autowired
    public ProfileService( UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUser(String email) {
        User user=userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        return user;
    }
    public User  updateUserProfile(String email, UpdateUserProfileRequest request ){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setBio(request.getBio());
        user.setGender(request.getGender());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setProfilePicture(request.getProfilePicture());
        user.setStatus(request.getStatus());
        return userRepository.save(user);
    }
}
