package com.example.backend_chat.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateUserProfileRequest {
    @Size(max = 50)
    @NotBlank
    private String firstName;

    @Size(max = 50)
    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;
    private String phoneNumber;
    private String address;
    private String status;
    private String gender;
    private String profilePicture;
    @Size(max = 50)
    private String bio;
}
