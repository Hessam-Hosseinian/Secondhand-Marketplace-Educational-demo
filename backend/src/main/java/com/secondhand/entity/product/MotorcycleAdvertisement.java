package com.secondhand.entity.product;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("MOTORCYCLE") @Getter @Setter @NoArgsConstructor
public class MotorcycleAdvertisement extends Advertisement {
  private String brand;
  private Integer modelYear;
  private Integer engineCapacity;
  private Long mileage;
  @Override public String productType() { return "MOTORCYCLE"; }
}
