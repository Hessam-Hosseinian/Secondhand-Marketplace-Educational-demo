package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("HOME_APPLIANCE") @Getter @Setter @NoArgsConstructor
public class HomeApplianceCategory extends HomeCategory {
  private Boolean energyRatingSupported = true;
  @Override public String categoryType() { return "HOME_APPLIANCE"; }
}
