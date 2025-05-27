package com.example.backend_chat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String secondName;

    @Email
    @Column(nullable = false, unique = true)
    private String email;


    @Column(nullable = false)
    private String passwordHash;

    private String profilePicture;

    private LocalDateTime createdAt = LocalDateTime.now();

    private Boolean isOnline = false;

    public User() {
    }

    public User( LocalDateTime createdAt, String email, String firstName, Long id, Boolean isOnline, String passwordHash, String profilePicture, String secondName ) {
        this.createdAt = createdAt;
        this.email = email;
        this.firstName = firstName;
        this.id = id;
        this.isOnline = isOnline;
        this.passwordHash = passwordHash;
        this.profilePicture = profilePicture;
        this.secondName = secondName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt( LocalDateTime createdAt ) {
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail( String email ) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName( String firstName ) {
        this.firstName = firstName;
    }

    public Long getId() {
        return id;
    }

    public void setId( Long id ) {
        this.id = id;
    }

    public Boolean getOnline() {
        return isOnline;
    }

    public void setOnline( Boolean online ) {
        isOnline = online;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash( String passwordHash ) {
        this.passwordHash = passwordHash;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture( String profilePicture ) {
        this.profilePicture = profilePicture;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName( String secondName ) {
        this.secondName = secondName;
    }
}