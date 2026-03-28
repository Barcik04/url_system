package com.example.url_system.repositories;

import com.example.url_system.models.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {

}
