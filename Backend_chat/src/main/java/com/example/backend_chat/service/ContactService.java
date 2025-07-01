package com.example.backend_chat.service;

import com.example.backend_chat.DTO.ContactRequest;
import com.example.backend_chat.DTO.ContactResponse;
import com.example.backend_chat.DTO.SimpleUserDTO;
import com.example.backend_chat.Notification.NotificationPayload;
import com.example.backend_chat.Notification.NotificationService;
import com.example.backend_chat.Notification.NotificationType;
import com.example.backend_chat.model.Contact;
import com.example.backend_chat.model.ENUM.ContactStatus;
import com.example.backend_chat.model.User;
import com.example.backend_chat.repository.ContactRepository;
import com.example.backend_chat.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContactService {
    private static final Logger logger = LoggerFactory.getLogger(ContactService.class);
    private final UserRepository userRepository;
    private final ContactRepository contactRepository;
    private final NotificationService notificationService;
  @Autowired
    public ContactService(ContactRepository contactRepository, UserRepository userRepository, NotificationService notificationService) {
        this.notificationService = notificationService;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    public ContactResponse sendFriendRequest(ContactRequest contact) {
        User sender = userRepository.findByEmail(contact.getSender())
                .orElseThrow(() -> new IllegalArgumentException("Sender not found"));
        User receiver = userRepository.findByEmail(contact.getReceiver())
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found"));
        if (sender.equals(receiver)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself.");
        }

        if (contactRepository.findByUserAndContact(sender, receiver).isPresent() ||
                contactRepository.findByUserAndContact(receiver, sender).isPresent()) {
            throw new IllegalArgumentException("Friend request already exists.");
        }

        Contact newContact = Contact.builder()
                .user(sender)
                .contact(receiver)
                .status(ContactStatus.PENDING)
                .build();
        contactRepository.save(newContact);

        ContactResponse contactResponse = toContactResponse(newContact);
        notificationService.sendToUser(receiver.getEmail(), NotificationPayload.builder()
                .message(sender.getFirstName() + " sent you a friend request")
                .type(NotificationType.FRIEND_REQUEST)
                .data(contactResponse)
                .build());

//        messageTemplate.convertAndSend("/topic/friend-request", contactResponse);
        logger.info("Sent friend request from {} to {}", sender.getEmail(), receiver.getEmail());
        return contactResponse;
    }

    public List<ContactResponse> getAcceptedRequest(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + email));
        List<SimpleUserDTO> friends = contactRepository.findAcceptedFriends(user);
        return friends.stream()
                .map(friend -> ContactResponse.builder()
                        .contact(friend)
                        .status(ContactStatus.valueOf(ContactStatus.ACCEPTED.name()))
                        .build())
                .toList();
    }

    public List<ContactResponse> getPendingRequest( String userEmail ) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userEmail));

        List<Contact> contacts = contactRepository.findByContactAndStatus(user, ContactStatus.PENDING);
        return contacts.stream().map(contact -> ContactResponse.builder()
                        .id(contact.getId())
                        .contact(toSimpleUser(contact.getUser()))
                        .status(contact.getStatus())
//                        .createdAt(contact.getCreatedAt())
                        .build())
                .toList();
    }

    public ContactResponse respondToRequest(String from, String action) {
        String receiverEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Processing friend request response for sender: {}, action: {}, receiver: {}", from, action, receiverEmail);

        User sender = userRepository.findByEmail(from)
                .orElseThrow(() -> new IllegalArgumentException("Sender not found: " + from));
        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found: " + receiverEmail));

        Contact contact = contactRepository.findByUserAndContact(sender, receiver)
                .orElseThrow(() -> new RuntimeException("Contact request not found for sender: " + from));

        if (contact.getStatus() != ContactStatus.PENDING) {
            throw new RuntimeException("Request already responded to: " + from);
        }

        ContactStatus newStatus = ContactStatus.valueOf(action.toUpperCase());
        if (newStatus != ContactStatus.ACCEPTED && newStatus != ContactStatus.REJECTED) {
            throw new IllegalArgumentException("Invalid action: " + action);
        }

        contact.setStatus(newStatus);
        contactRepository.save(contact);

        ContactResponse response = toContactResponse(contact);
        notificationService.sendToUser(sender.getEmail(), NotificationPayload.builder()
//                .title("Friend Request Response")
                .message("Your request was " + newStatus.name().toLowerCase())
                .type(newStatus == ContactStatus.ACCEPTED ? NotificationType.FRIEND_REQUEST_ACCEPTED : NotificationType.FRIEND_REQUEST_REJECTED)
                .data(response)
                .build());
        logger.info("Sent response {} to {}", newStatus, from);

        if (newStatus == ContactStatus.REJECTED) {
            contactRepository.delete(contact);
        }
        return response;
    }

    private SimpleUserDTO toSimpleUser(User user) {
        return SimpleUserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .isOnline(user.getIsOnline())
                .build();
    }

    private ContactResponse toContactResponse(Contact contact) {
        return ContactResponse.builder()
                .id(contact.getId())
                .contact(toSimpleUser(contact.getContact()))
                .status(ContactStatus.valueOf(contact.getStatus().name()))
                .build();
    }

    public void rejectRequest( String rejectedSenderEmail ) {
        String receiverEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Contact contact = contactRepository.findByUserEmailAndContactEmail(rejectedSenderEmail, receiverEmail)
                .orElseThrow(() -> new RuntimeException("Contact request not found for sender: " +rejectedSenderEmail));

        contactRepository.delete(contact);
        // notify sender with response
        notificationService.sendToUser(rejectedSenderEmail,
                NotificationPayload.builder()
                        .message(rejectedSenderEmail+"reject your request 😢")
                        .type(NotificationType.FRIEND_REQUEST_REJECTED)
                        .build());
        // notify receiver reject is done
        notificationService.sendToUser(receiverEmail,
                NotificationPayload.builder()
                        .message("request is rejected 😢")
                        .type(NotificationType.FRIEND_REQUEST_REJECTED)
                        .build());
    }

    public void acceptRequest( String acceptedSenderEmail ) {
        String receiverEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        Contact contact = contactRepository.findByUserEmailAndContactEmail(acceptedSenderEmail, receiverEmail)
                .orElseThrow(() -> new RuntimeException("Contact request not found for sender: " +acceptedSenderEmail));

        contact.setStatus(ContactStatus.ACCEPTED);
        contactRepository.save(contact);
        // notify sender with response
        notificationService.sendToUser(acceptedSenderEmail,
                NotificationPayload.builder()
                        .message(acceptedSenderEmail+"accepted your request 🎉")
                        .type(NotificationType.FRIEND_REQUEST_ACCEPTED)
                        .build());
        // notify receiver reject is done
        notificationService.sendToUser(receiverEmail,
                NotificationPayload.builder()
                        .message("request is accepted ✅")
                        .type(NotificationType.FRIEND_REQUEST_ACCEPTED)
                        .build());
    }
}