package com.secondhand.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "advertisement_id", "attribute_key" }))
public class AdvertisementAttribute {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Advertisement advertisement;

  @Column(name = "attribute_key", nullable = false)
  private String attributeKey;

  @Column(nullable = false)
  private String label;

  @Column(nullable = false, length = 1000)
  private String value;
}
