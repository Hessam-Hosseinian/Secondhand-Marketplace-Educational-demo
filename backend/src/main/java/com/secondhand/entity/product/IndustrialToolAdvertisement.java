package com.secondhand.entity.product;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("INDUSTRIAL_TOOL") @Getter @Setter @NoArgsConstructor
public class IndustrialToolAdvertisement extends Advertisement {
  private String brand;
  private String power;
  private String voltage;
  private String safetyClass;
  @Override public String productType() { return "INDUSTRIAL_TOOL"; }
}
