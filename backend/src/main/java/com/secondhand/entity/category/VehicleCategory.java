package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("VEHICLE") @Getter @Setter @NoArgsConstructor
public class VehicleCategory extends Category {
  private Boolean registrationRequired = true;
  @Override public String categoryType() { return "VEHICLE"; }
}
