package com.example.backend_chat.repository;

import com.example.backend_chat.model.Contact;
import com.example.backend_chat.model.ENUM.ContactStatus;
import com.example.backend_chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    Optional<Contact> findByUserAndContact( User user, User contact);
    Optional<Contact> findById( Long id);
    List<Contact> findByUserAndStatus( User user, ContactStatus status);
    List<Contact> findByContactAndStatus( User user, ContactStatus contactStatus );
}
