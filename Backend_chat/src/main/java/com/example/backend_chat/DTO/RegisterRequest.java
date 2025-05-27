package com.example.backend_chat.DTO;

public class RegisterRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    public RegisterRequest(String firstName, String lastName, String email, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
    }
}
