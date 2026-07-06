package com.secondhand.entity.product;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("CAR") @Getter @Setter @NoArgsConstructor
public class CarAdvertisement extends Advertisement {
  private String brand;
  private Integer modelYear;
  private Long mileage;
  private String fuelType;
  private String transmission;
  @Override public String productType() { return "CAR"; }
}
