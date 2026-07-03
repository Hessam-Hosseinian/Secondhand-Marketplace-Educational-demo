package com.secondhand.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  private Category parent;

  @Column(nullable = false)
  private boolean active = true;
}
