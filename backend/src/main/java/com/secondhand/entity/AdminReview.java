package com.secondhand.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AdminReview {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Advertisement advertisement;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private User admin;

  @Enumerated(EnumType.STRING)
  private Enums.ReviewDecision decision;

  private String note;
  private LocalDateTime createdAt = LocalDateTime.now();
}
