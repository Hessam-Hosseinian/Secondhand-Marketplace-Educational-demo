package com.secondhand.entity.product;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("LAPTOP") @Getter @Setter @NoArgsConstructor
public class LaptopAdvertisement extends Advertisement {
  private String brand;
  private String cpu;
  private String gpu;
  private String ram;
  private String storage;
  @Override public String productType() { return "LAPTOP"; }
}
