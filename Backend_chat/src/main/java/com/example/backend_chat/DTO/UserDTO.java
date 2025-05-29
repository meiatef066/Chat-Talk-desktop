package com.example.backend_chat.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private String status;
    private String gender;
    private String profilePicture;
    private String bio;
}
