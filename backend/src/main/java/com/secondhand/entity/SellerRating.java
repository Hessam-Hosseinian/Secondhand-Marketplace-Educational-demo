package com.secondhand.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Entity
@Table(
  uniqueConstraints = @UniqueConstraint(
    columnNames = { "advertisement_id", "buyer_id", "seller_id" }
  )
)
@Getter
@Setter
@NoArgsConstructor
public class SellerRating {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Advertisement advertisement;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private User seller;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private User buyer;

  @Column(nullable = false)
  private int rating;

  private String comment;
  private LocalDateTime createdAt = LocalDateTime.now();
}
