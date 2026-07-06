package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("MOTORCYCLE") @Getter @Setter @NoArgsConstructor
public class MotorcycleCategory extends VehicleCategory {
  private Boolean engineCapacityRequired = true;
  @Override public String categoryType() { return "MOTORCYCLE"; }
}
