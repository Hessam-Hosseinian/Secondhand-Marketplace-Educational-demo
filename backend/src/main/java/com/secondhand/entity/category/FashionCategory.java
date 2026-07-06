package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("FASHION") @Getter @Setter @NoArgsConstructor
public class FashionCategory extends Category {
  private Boolean sizeSupported = true;
  @Override public String categoryType() { return "FASHION"; }
}
