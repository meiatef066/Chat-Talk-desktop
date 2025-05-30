package com.example.backend_chat.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Setter
@Getter
public class UpdateUserProfileRequest {
    @Size(max = 50)
    private String firstName;
    @Size(max = 50)
    private String lastName;
    @Email
    private String email;
    private String phoneNumber;
    private String address;
    private String country;
    private String gender;
    private MultipartFile profilePicture;
    @Size(max = 50)
    private String bio;
}
