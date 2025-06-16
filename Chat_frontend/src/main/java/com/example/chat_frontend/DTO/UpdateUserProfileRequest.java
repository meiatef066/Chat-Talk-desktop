package com.example.chat_frontend.DTO;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.File;


@Setter
@Getter
@Data
public class UpdateUserProfileRequest {
    // Getters and Setters
    private String firstName;
    private String lastName;
    private String email;
    private String bio;
    private String gender;
    private String phoneNumber;
    private String country;
    private File profilePicture;

    // Constructors
    public UpdateUserProfileRequest() {}

}