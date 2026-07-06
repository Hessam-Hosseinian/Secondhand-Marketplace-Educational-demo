package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("ELECTRONICS") @Getter @Setter @NoArgsConstructor
public class ElectronicsCategory extends Category {
  private Boolean warrantySupported = true;
  @Override public String categoryType() { return "ELECTRONICS"; }
}
