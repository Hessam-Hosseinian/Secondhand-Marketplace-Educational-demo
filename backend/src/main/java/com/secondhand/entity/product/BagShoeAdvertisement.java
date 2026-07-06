package com.secondhand.entity.product;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("BAG_SHOE") @Getter @Setter @NoArgsConstructor
public class BagShoeAdvertisement extends Advertisement {
  private String brand;
  private String material;
  private String size;
  private String color;
  @Override public String productType() { return "BAG_SHOE"; }
}
