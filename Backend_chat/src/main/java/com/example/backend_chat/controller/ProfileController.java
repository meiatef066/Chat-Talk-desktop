package com.example.backend_chat.controller;

import com.example.backend_chat.DTO.UpdateUserProfileRequest;
import com.example.backend_chat.model.User;
import com.example.backend_chat.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;

    public ProfileController( ProfileService profileService ) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<User> getProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(">>> Controller Authentication: " + auth);
        System.out.println(">>> Auth Name: " + auth.getName());
        System.out.println(">>> Auth Principal: " + auth.getPrincipal());
        String username = auth.getName();
        User user = profileService.getUser(username);
        return ResponseEntity.ok(user);
    }


    @PatchMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<?> updateProfile(
            @ModelAttribute @Valid UpdateUserProfileRequest request ) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User updatedUser = profileService.updateUserProfile(email, request);
        return ResponseEntity.ok(updatedUser);

    }


}