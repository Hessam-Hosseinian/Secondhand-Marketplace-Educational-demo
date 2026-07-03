package com.secondhand.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AdvertisementImage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Advertisement advertisement;

  @Column(nullable = false, length = 1000)
  private String imageUrl;

  private Integer sortOrder = 0;
}
