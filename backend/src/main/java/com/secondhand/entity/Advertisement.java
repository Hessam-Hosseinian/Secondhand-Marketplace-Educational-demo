package com.secondhand.entity;

import jakarta.persistence.*;
import java.math.*;
import java.time.*;
import java.util.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Advertisement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 5000)
  private String description;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal price;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Enums.ItemCondition itemCondition;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Enums.AdStatus status = Enums.AdStatus.PENDING;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private User owner;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Category category;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private City city;

  @Column(length = 3000)
  private String attributesText;

  private String rejectionReason;
  private String adminNote;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  @OneToMany(
    mappedBy = "advertisement",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  @OrderBy("sortOrder")
  private List<AdvertisementImage> images = new ArrayList<>();

  @PreUpdate
  void update() {
    updatedAt = LocalDateTime.now();
  }
}
