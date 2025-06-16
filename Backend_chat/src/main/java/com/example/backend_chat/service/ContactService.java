package com.example.backend_chat.service;

import com.example.backend_chat.DTO.ContactRequest;
import com.example.backend_chat.DTO.ContactResponse;
import com.example.backend_chat.DTO.SimpleUserDTO;
import com.example.backend_chat.exception.FriendRequestAlreadyExist;
import com.example.backend_chat.model.Contact;
import com.example.backend_chat.model.ENUM.ContactStatus;
import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.ContactRepository;
import com.example.backend_chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class ContactService {
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final SimpMessagingTemplate messageTemplate;

    @Autowired
    public ContactService( ContactRepository contactRepository, UserRepository userRepository, SimpMessagingTemplate messageTemplate ) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.messageTemplate = messageTemplate;
    }

    public ContactResponse sendFriendRequest( ContactRequest contact ) {
        User sender = userRepository.findByEmail(contact.getSender())
                .orElseThrow(() -> new UsernameNotFoundException("Sender not found"));
        User receiver = userRepository.findByEmail(contact.getReceiver())
                .orElseThrow(() -> new UsernameNotFoundException("Receiver not found"));
        if (sender.equals(receiver)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself.");
        }

        if (contactRepository.findByUserAndContact(sender, receiver).isPresent() ||
                contactRepository.findByUserAndContact(receiver, sender).isPresent()) {
            throw new FriendRequestAlreadyExist("Friend request already exists.");
        }

        Contact newContact = Contact.builder().user(sender).contact(receiver).status(ContactStatus.PENDING).build();
        contactRepository.save(newContact);
        // notify receiver via websocket real time
        ContactResponse contactResponse= toContactResponse(newContact);
        messageTemplate.convertAndSendToUser(receiver.getEmail(),"/topic/friend-request", contactResponse);
        return contactResponse;
    }

    private ContactResponse toContactResponse( Contact contact ) {
        return ContactResponse.builder()
                .id(contact.getId())
                .user(toSimpleUser(contact.getUser()))
                .contact(toSimpleUser(contact.getContact()))
                .status(contact.getStatus().name())
//                .createdAt(contact.getCreatedAt())
                .build();
    }

    public List<ContactResponse> getAcceptedRequest( String email ) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<Contact> contactsAsUser = contactRepository.findByUserAndStatus(user, ContactStatus.ACCEPTED);
        List<Contact> contactsAsContact = contactRepository.findByContactAndStatus(user, ContactStatus.ACCEPTED);

        Set<Contact> allContacts = new HashSet<>();
        allContacts.addAll(contactsAsUser);
        allContacts.addAll(contactsAsContact);

        return allContacts.stream().map(this::toContactResponse).toList();
    }

    public List<ContactResponse> getRejectedRequest( Long id ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        List<Contact> contacts =  (contactRepository.findByUserAndStatus(user, ContactStatus.ACCEPTED));
        return contacts.stream().map(this::toContactResponse).toList();
    }

    public List<ContactResponse> getPendingRequest( String userEmail ) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        List<Contact> contacts = contactRepository.findByUserAndStatus(user, ContactStatus.PENDING);

        //return contacts.stream().map(this::toContactResponse).toList();
        return contacts.stream().map(contact -> ContactResponse.builder()
                        .id(contact.getId())
                        .user(toSimpleUser(contact.getUser()))
                        .contact(toSimpleUser(contact.getContact()))
                        .status(contact.getStatus().name())
//                        .createdAt(contact.getCreatedAt())
                        .build())
                .toList();
    }

    private SimpleUserDTO toSimpleUser( User user ) {
        return SimpleUserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .isOnline(user.getIsOnline())
                .build();
    }

    public void respondToRequest( String senderEmail, String receiverEmail, String action ) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Sender not found"));

        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Receiver not found"));

        Contact contact = contactRepository.findByUserAndContact(sender, receiver)
                .orElseThrow(() -> new RuntimeException("Contact request not found"));

        if (contact.getStatus() != ContactStatus.PENDING) {
            throw new RuntimeException("Request already responded to" + senderEmail);
        }

        ContactStatus newStatus;
        try {
            newStatus = ContactStatus.valueOf(action.toUpperCase());
            if (newStatus != ContactStatus.ACCEPTED && newStatus != ContactStatus.REJECTED && newStatus != ContactStatus.BLOCKED) {
                throw new IllegalArgumentException("Invalid action.");
            }
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid action: " + action, e);
        }


        contact.setStatus(newStatus);
        contactRepository.save(contact);

        messageTemplate.convertAndSendToUser(
                senderEmail,
                "/topic/friend-request-response",
                toContactResponse(contact)
        );
        if (newStatus == ContactStatus.ACCEPTED) {
            Contact reciprocalContact = Contact.builder()
                    .user(receiver)
                    .contact(sender)
                    .status(ContactStatus.ACCEPTED)
                    .build();
            contactRepository.save(reciprocalContact);

            // Notify receiver of new friendship
            messageTemplate.convertAndSendToUser(
                    receiver.getEmail(),
                    "/topic/friend-request-response",
                    toContactResponse(reciprocalContact)
            );
        }

    }
}
