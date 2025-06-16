package com.example.backend_chat.controller;

import com.example.backend_chat.DTO.ContactRequest;
import com.example.backend_chat.DTO.ContactResponse;
import com.example.backend_chat.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST controller for managing contact (friend) requests.
 */
@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private final ContactService contactService;

    @Autowired
    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }
    /**
     * Send a new friend request to another user.
     * post /api/contacts
     * @param contactRequest the contact request containing recipient's email
     * @return the created Contact entity
     */
    @PostMapping
    public ResponseEntity<ContactResponse> addContact( @RequestBody ContactRequest contactRequest ) {
        String senderEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        contactRequest.setSender(senderEmail);
        ContactResponse contact = contactService.sendFriendRequest(contactRequest);
        return ResponseEntity.ok(contact);
    }
    @GetMapping("/pending")
    public ResponseEntity<List<ContactResponse>> getPendingContacts() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<ContactResponse> contacts = contactService.getPendingRequest(email);
            return ResponseEntity.ok(contacts);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(404).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    @GetMapping("/accepted")
    public ResponseEntity<List<ContactResponse>> getAcceptedContacts() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            List<ContactResponse> contacts = contactService.getAcceptedRequest(email);
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    /**
     * Retrieve all pending friend requests sent by the currently authenticated user.
     * get /api/contacts  {token }
     * @return list of ContactResponse DTOs representing pending requests
     */
    //  Get all pending requests sent by authenticated user
    @GetMapping
    public ResponseEntity<List<ContactResponse>> getAllContacts(@RequestBody String message) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        List<ContactResponse> contacts = contactService.getPendingRequest(email);
//        List<ContactResponse> contacts = contactService.getAcceptedRequest(email);
        System.out.println("Authenticated user email: " + email);
        try {
            List<ContactResponse> contacts = contactService.getPendingRequest(email);
            System.out.println("Fetched " + contacts.size() + " pending requests.");
            return ResponseEntity.ok(contacts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    /**
     * Respond to a friend request (accept or reject).
     *  senderEmail the email of the user who sent the friend request
     * @param action the action to take ("accept" or "reject")
     * @return success message indicating the result
     */

    @PatchMapping("/{receiverEmail}/response")
    public ResponseEntity<String> respondToContactRequest( @PathVariable String receiverEmail, @RequestParam("action") String action ) {

        String senderEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        contactService.respondToRequest(senderEmail, receiverEmail, action);
        return ResponseEntity.ok("Request " + action.toLowerCase());
    }

//    private ContactResponse toContactResponse(Contact contact) {
//        return ContactResponse.builder()
//                .id(contact.getId())
//                .user(toSimpleUser(contact.getUser()))
//                .contact(toSimpleUser(contact.getContact()))
//                .status(contact.getStatus().name())
//                .createdAt(contact.getCreatedAt())
//                .build();
//    }
//    private SimpleUserDTO toSimpleUser( User user) {
//        return SimpleUserDTO.builder()
//                .id(user.getId())
//                .firstName(user.getFirstName())
//                .lastName(user.getLastName())
//                .email(user.getEmail())
//                .profilePicture(user.getProfilePicture())
//                .isOnline(user.getIsOnline())
//                .build();
//    }
}
