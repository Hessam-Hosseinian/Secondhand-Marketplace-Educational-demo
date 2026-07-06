package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("LAPTOP") @Getter @Setter @NoArgsConstructor
public class LaptopCategory extends ElectronicsCategory {
  private Boolean hardwareSpecsRequired = true;
  @Override public String categoryType() { return "LAPTOP"; }
}
