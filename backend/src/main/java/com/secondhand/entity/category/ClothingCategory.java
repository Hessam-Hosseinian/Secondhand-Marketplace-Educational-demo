package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("CLOTHING") @Getter @Setter @NoArgsConstructor
public class ClothingCategory extends FashionCategory {
  private Boolean sizeRequired = true;
  @Override public String categoryType() { return "CLOTHING"; }
}
