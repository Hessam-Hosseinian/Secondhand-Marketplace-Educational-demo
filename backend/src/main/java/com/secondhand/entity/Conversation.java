package com.secondhand.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import lombok.*;

@Entity
@Table(
  uniqueConstraints = @UniqueConstraint(
    columnNames = { "advertisement_id", "buyer_id" }
  )
)
@Getter
@Setter
@NoArgsConstructor
public class Conversation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Advertisement advertisement;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private User buyer;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private User seller;

  private LocalDateTime createdAt = LocalDateTime.now();

  @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL)
  @OrderBy("createdAt")
  private List<Message> messages = new ArrayList<>();
}
