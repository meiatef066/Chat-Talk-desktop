package com.example.backend_chat.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.backend_chat.DTO.UpdateUserProfileRequest;
import com.example.backend_chat.model.User;
import com.example.backend_chat.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {
private final Cloudinary cloudinary;
    private final ProfileService profileService;
    public ProfileController( ProfileService profileService, Cloudinary cloudinary ) {
        this.cloudinary = cloudinary;
        this.profileService = profileService;
    }
    @GetMapping
    public ResponseEntity<User> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user=profileService.getUser(username);
        return ResponseEntity.ok(user);
    }

    @PatchMapping
    public ResponseEntity<?> updateProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User updatedUser = profileService.updateUserProfile(email, request);
        return ResponseEntity.ok(updatedUser);

    }



}