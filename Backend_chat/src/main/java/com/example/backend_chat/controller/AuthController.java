package com.example.backend_chat.controller;

import com.example.backend_chat.DTO.AuthResponse;
import com.example.backend_chat.DTO.LoginRequest;
import com.example.backend_chat.DTO.RegisterRequest;
import com.example.backend_chat.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService ) {
        this.authService = authService;
    }
    @GetMapping
    public String  hello(){
        return "hello world";
    }
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.createUser(request);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "User registered successfully");
        body.put("token", response.getToken());

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Login successful");
        body.put("token", response.getToken());

        return ResponseEntity.ok(body); // HTTP 200
    }


}
