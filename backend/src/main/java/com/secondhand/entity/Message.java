package com.secondhand.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Message {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Conversation conversation;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private User sender;

  @Column(nullable = false, length = 2000)
  private String content;

  private LocalDateTime createdAt = LocalDateTime.now();
  private boolean read;
}
