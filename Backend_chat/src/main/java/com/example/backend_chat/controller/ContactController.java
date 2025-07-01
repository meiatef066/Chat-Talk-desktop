package com.example.backend_chat.controller;

import com.example.backend_chat.DTO.ContactRequest;
import com.example.backend_chat.DTO.ContactResponse;
import com.example.backend_chat.service.ContactService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private final ContactService contactService;

    public ContactController( ContactService contactService ) {
        this.contactService = contactService;
    }

    @PostMapping("/request")
    public ResponseEntity<ContactResponse> addContact( @RequestBody ContactRequest contactRequest ) {
        String senderEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        contactRequest.setSender(senderEmail);
        try {
            ContactResponse contact = contactService.sendFriendRequest(contactRequest);
            return ResponseEntity.ok(contact);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @GetMapping("/request/pending")
    public ResponseEntity<List<ContactResponse>> getPendingContacts() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<ContactResponse> contacts = contactService.getPendingRequest(email);
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/request/accepted")
    public ResponseEntity<List<ContactResponse>> getAcceptedContacts() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<ContactResponse> contacts = contactService.getAcceptedRequest(email);
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @PatchMapping("/{senderRequestEmail}/response")
    public ResponseEntity<ContactResponse> respondToContactRequest( @PathVariable String senderRequestEmail, @RequestParam("action") String action ) {
        try {
            ContactResponse contactResponse = contactService.respondToRequest(senderRequestEmail, action);
            return ResponseEntity.ok(contactResponse);
        } catch (Exception e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @PatchMapping("/{senderRequestEmail}")
    public ResponseEntity<String> acceptRequest( @PathVariable String senderRequestEmail ) {
        contactService.acceptRequest(senderRequestEmail);
        return ResponseEntity.ok("✅ Friend request accepted successfully.");
    }

    @DeleteMapping("/{senderRequestEmail}")
    public ResponseEntity<String> rejectRequest( @PathVariable String senderRequestEmail ) {
        contactService.rejectRequest(senderRequestEmail);
        return ResponseEntity.ok("❌ Friend request rejected successfully");
    }
}