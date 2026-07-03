package com.secondhand.repository;

import com.secondhand.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Long> {
  List<Message> findByConversationOrderByCreatedAt(Conversation c);
}
