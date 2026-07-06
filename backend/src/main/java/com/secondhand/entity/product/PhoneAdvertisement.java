package com.secondhand.entity.product;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("PHONE") @Getter @Setter @NoArgsConstructor
public class PhoneAdvertisement extends Advertisement {
  private String brand;
  private String storage;
  private String ram;
  private Integer batteryHealth;
  private Boolean registered;
  @Override public String productType() { return "PHONE"; }
}
