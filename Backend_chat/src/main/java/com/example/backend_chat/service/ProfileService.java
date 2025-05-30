package com.example.backend_chat.service;


import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.backend_chat.DTO.UpdateUserProfileRequest;
import com.example.backend_chat.exception.UserNotFoundException;
import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ProfileService {
    private final Cloudinary cloudinary;
    private final UserRepository userRepository ;
    @Autowired
    public ProfileService( UserRepository userRepository, Cloudinary cloudinary) {
        this.userRepository = userRepository;
        this.cloudinary = cloudinary;
    }

    public User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }
    public User updateUserProfile(String email, UpdateUserProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfilePicture() != null && !request.getProfilePicture().isEmpty()) {
            try {
                MultipartFile file = request.getProfilePicture();
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
                String imageUrl = (String) uploadResult.get("secure_url");
                user.setProfilePicture(imageUrl);// Set uploaded image URL
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image", e);
            }
        }
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }

        return userRepository.save(user);
    }

}
