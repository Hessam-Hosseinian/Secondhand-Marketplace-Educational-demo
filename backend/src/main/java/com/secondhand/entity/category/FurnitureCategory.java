package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("FURNITURE") @Getter @Setter @NoArgsConstructor
public class FurnitureCategory extends HomeCategory {
  private Boolean materialRequired = true;
  @Override public String categoryType() { return "FURNITURE"; }
}
