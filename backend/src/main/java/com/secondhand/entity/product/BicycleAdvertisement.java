package com.secondhand.entity.product;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("BICYCLE") @Getter @Setter @NoArgsConstructor
public class BicycleAdvertisement extends Advertisement {
  private String brand;
  private String frameSize;
  private String wheelSize;
  private Integer gearCount;
  @Override public String productType() { return "BICYCLE"; }
}
