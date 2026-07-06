package com.secondhand.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "category_id", "attribute_key" }))
public class CategoryAttribute {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Category category;

  @Column(name = "attribute_key", nullable = false)
  private String attributeKey;

  @Column(nullable = false)
  private String label;

  @Column(nullable = false)
  private String inputType = "TEXT";

  private String optionsText;

  @Column(nullable = false)
  private boolean required;

  @Column(nullable = false)
  private int sortOrder;
}
