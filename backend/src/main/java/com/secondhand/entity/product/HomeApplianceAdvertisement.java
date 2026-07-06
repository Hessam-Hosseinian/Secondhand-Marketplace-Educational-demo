package com.secondhand.entity.product;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("HOME_APPLIANCE") @Getter @Setter @NoArgsConstructor
public class HomeApplianceAdvertisement extends Advertisement {
  private String brand;
  private String energyRating;
  private String capacity;
  private Integer warrantyMonths;
  @Override public String productType() { return "HOME_APPLIANCE"; }
}
