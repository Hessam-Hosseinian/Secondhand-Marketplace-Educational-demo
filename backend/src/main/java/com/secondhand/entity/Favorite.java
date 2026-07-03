package com.secondhand.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Entity
@Table(
  uniqueConstraints = @UniqueConstraint(
    columnNames = { "user_id", "advertisement_id" }
  )
)
@Getter
@Setter
@NoArgsConstructor
public class Favorite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private User user;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Advertisement advertisement;

  private LocalDateTime createdAt = LocalDateTime.now();
}
