package com.example.backend_chat.controller;

import com.example.backend_chat.DTO.ContactResponse;
import com.example.backend_chat.DTO.UserSearchDTO;
import com.example.backend_chat.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/users")
public class UsersController {
    private final UsersService userService;
    @Autowired
    public UsersController( UsersService userService) {
        this.userService = userService;
    }
    @GetMapping("/search")
    public ResponseEntity<List<ContactResponse>> searchUsers( @RequestParam("query") String query) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return ResponseEntity.ok(userService.searchUser_v2(query,email));
    }

}
