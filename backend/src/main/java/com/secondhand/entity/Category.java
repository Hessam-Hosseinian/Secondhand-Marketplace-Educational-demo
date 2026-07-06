package com.secondhand.entity;

import com.secondhand.entity.base.BaseCategory;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DiscriminatorFormula;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("coalesce(category_type, 'GENERIC')")
@DiscriminatorValue("GENERIC")
@Getter
@Setter
@NoArgsConstructor
public class Category extends BaseCategory {
  @Column(name = "category_type")
  private String categoryKind = "GENERIC";

  @ManyToOne(fetch = FetchType.LAZY)
  private Category parent;

  @Override
  public String categoryType() {
    return "GENERIC";
  }
}
