package com.example.backend_chat.repository;

import com.example.backend_chat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
 //avoid NullPointerExceptions and makes your code more readable and safer
    Optional<User> findByEmail( String email);
}