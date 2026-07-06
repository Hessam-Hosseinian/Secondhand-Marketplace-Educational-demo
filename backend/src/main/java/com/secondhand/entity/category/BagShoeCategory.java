package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("BAG_SHOE") @Getter @Setter @NoArgsConstructor
public class BagShoeCategory extends FashionCategory {
  private Boolean materialSupported = true;
  @Override public String categoryType() { return "BAG_SHOE"; }
}
