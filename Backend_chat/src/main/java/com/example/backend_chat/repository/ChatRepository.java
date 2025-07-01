package com.example.backend_chat.repository;

import com.example.backend_chat.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat,Long> {
    @Query("""
select c from Chat c 
JOIN c.participants p1
join c.participants p2
where c.isGroup= false
AND p1.user.email=:email1
AND p2.user.email=:email2
""")
    Optional<Chat> findPrivateChatBetweenUsers( String email1, String email2);

}
