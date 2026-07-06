package com.secondhand.entity;

import com.secondhand.entity.base.BaseProduct;
import jakarta.persistence.*;
import java.util.*;
import lombok.*;
import org.hibernate.annotations.DiscriminatorFormula;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("coalesce(product_type, 'GENERIC')")
@DiscriminatorValue("GENERIC")
@Getter
@Setter
@NoArgsConstructor
public class Advertisement extends BaseProduct {
  @Column(name = "product_type")
  private String productKind = "GENERIC";

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

  @OneToMany(
    mappedBy = "advertisement",
    cascade = CascadeType.ALL,
    orphanRemoval = true
  )
  @OrderBy("sortOrder")
  private List<AdvertisementImage> images = new ArrayList<>();

  @OneToMany(mappedBy = "advertisement", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("id")
  private List<AdvertisementAttribute> attributes = new ArrayList<>();

  @Override
  public String productType() {
    return "GENERIC";
  }
}
