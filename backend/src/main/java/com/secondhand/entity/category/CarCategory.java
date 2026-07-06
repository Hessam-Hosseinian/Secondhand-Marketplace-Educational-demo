package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("CAR") @Getter @Setter @NoArgsConstructor
public class CarCategory extends VehicleCategory {
  private Boolean mileageRequired = true;
  @Override public String categoryType() { return "CAR"; }
}
