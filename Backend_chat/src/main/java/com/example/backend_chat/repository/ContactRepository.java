package com.example.backend_chat.repository;

import com.example.backend_chat.model.Contact;
import com.example.backend_chat.model.ENUM.ContactStatus;
import com.example.backend_chat.model.User;
import com.example.backend_chat.DTO.SimpleUserDTO;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    Optional<Contact> findByUserAndContact( User user, User contact);
    Optional<Contact> findById( Long id);
    List<Contact> findByUserAndStatus( User user, ContactStatus status);
    List<Contact> findByContactAndStatus( User user, ContactStatus contactStatus );
//    @Query("SELECT c FROM Contact c WHERE c.user.email = :email OR c.contact.email = :email")
    Optional<Contact>  findByUserEmailAndContactEmail(@Email String currentUserEmail, @Email String email );
    @Query("SELECT c FROM Contact c WHERE " +
            "(c.user.email = :currentUserEmail AND c.contact.email IN :targetEmails) OR " +
            "(c.contact.email = :currentUserEmail AND c.user.email IN :targetEmails)")
    List<Contact> findAllContactsBetweenUserAndTargetUsers(@Param("currentUserEmail") String currentUserEmail,
                                                           @Param("targetEmails") List<String> targetEmails);

    @Query("SELECT new com.example.backend_chat.DTO.SimpleUserDTO(c.contact.id, c.contact.firstName, c.contact.lastName, c.contact.email, c.contact.profilePicture, c.contact.isOnline) " +
            "FROM Contact c WHERE c.user = :user AND c.status = 'ACCEPTED' " +
            "UNION " +
            "SELECT new com.example.backend_chat.DTO.SimpleUserDTO(c.user.id, c.user.firstName, c.user.lastName, c.user.email, c.user.profilePicture, c.user.isOnline) " +
            "FROM Contact c WHERE c.contact = :user AND c.status = 'ACCEPTED'")
    List<SimpleUserDTO> findAcceptedFriends(@Param("user") User user);

    Contact findByUserEmail( String rejectedEmail );
}
