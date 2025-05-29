package com.example.backend_chat.model;

import com.example.backend_chat.model.ENUM.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import jakarta.validation.constraints.Email;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Builder
@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name",nullable = false)
    private String firstName;

    @Column(name = "second_name",nullable = false)
    private String lastName;

    @Email
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name="password_hash",nullable = false)
    private String password;
    @Column(name = "profile_picture")
    private String profilePicture;
@Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
@Column(name = "is_online")
    private Boolean isOnline = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role = Role.USER;
@Column(name = "bio")
    private String bio;
@Column(name = "phone_number")
    private String phoneNumber;
@Column(name = "country")
    private String country;
@Column(name = "gender")
    private String gender;

    // SPRING SECURITY OVERRIDES

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.isOnline = false;
        if (this.role == null) {
            this.role = Role.USER;
        }
    }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
