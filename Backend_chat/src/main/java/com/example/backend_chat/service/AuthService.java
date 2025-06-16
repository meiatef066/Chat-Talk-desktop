package com.example.backend_chat.service;

import com.example.backend_chat.DTO.AuthResponse;
import com.example.backend_chat.DTO.LoginRequest;
import com.example.backend_chat.DTO.RegisterRequest;
import com.example.backend_chat.exception.EmailAlreadyExistsException;
import com.example.backend_chat.exception.UserNotFoundException;
import com.example.backend_chat.model.ENUM.Role;
import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.UserRepository;
import com.example.backend_chat.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 📁 7. service/
 * 🔹 AuthService.java
 * Register:
 * <p>
 * Validates email uniqueness
 * <p>
 * Hashes password
 * <p>
 * Saves user
 * <p>
 * Returns JWT
 * <p>
 * Login:
 * <p>
 * Uses AuthenticationManager
 * <p>
 * Validates user
 * <p>
 * Returns JWT
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // create account
    public AuthResponse createUser( RegisterRequest request ) {
        if(userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Email already in use: " + request.getEmail());
        }
        // hash password
        passwordEncoder.encode(request.getPassword());
        User user = new User();
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        userRepository.save(user);

        String token = jwtUtil.generateToken(new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER"))
        ));

        return new AuthResponse(token);
    }

    public AuthResponse login( LoginRequest loginRequest ) {
        Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword())) {
            throw new BadCredentialsException("Bad credentials");
        }

        String token = jwtUtil.generateToken(user.get());
        return new AuthResponse(token);
    }

}
