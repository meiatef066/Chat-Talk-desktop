package com.example.chat_frontend.Model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String firstname;
    private String lastname;
    private String email;
    private String phone;
    private String Country;
    private String ProfilePicture;
    private String password;

    public User(String firstName, String lastName, String email, String password) {
        this.firstname = firstName;
        this.lastname = lastName;
        this.email = email;
        this.password = password;
    }
    public User(String firstName, String lastName, String ProfilePicture) {
        this.firstname = firstName;
        this.lastname = lastName;
        this.ProfilePicture = ProfilePicture;
    }
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    @JsonProperty("firstName")
    public String getFirstName() {
        return firstname;
    }

    @JsonProperty("lastName")
    public String getLastName() {
        return lastname;
    }
    @JsonProperty("ProfilePicture")
    public String getProfilePicture() {
        return ProfilePicture;
    }
    @JsonProperty("email")
    public String getEmail() {
        return email;
    }
    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "User [firstname=" + firstname + ", lastname=" + lastname + ", email=" + email;
    }
}
