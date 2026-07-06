package com.secondhand.entity.product;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("CLOTHING") @Getter @Setter @NoArgsConstructor
public class ClothingAdvertisement extends Advertisement {
  private String size;
  private String color;
  private String material;
  private String targetGender;
  @Override public String productType() { return "CLOTHING"; }
}
