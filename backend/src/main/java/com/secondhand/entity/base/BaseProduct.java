package com.secondhand.entity.base;

import com.secondhand.entity.Enums;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.*;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseProduct {
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

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @Column(nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  public abstract String productType();

  @PreUpdate
  void touch() {
    updatedAt = LocalDateTime.now();
  }
}
