package com.secondhand.entity.product;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("FURNITURE") @Getter @Setter @NoArgsConstructor
public class FurnitureAdvertisement extends Advertisement {
  private String material;
  private String color;
  private String dimensions;
  private Integer pieceCount;
  @Override public String productType() { return "FURNITURE"; }
}
