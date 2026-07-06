package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("PHONE") @Getter @Setter @NoArgsConstructor
public class PhoneCategory extends ElectronicsCategory {
  private Boolean registryRequired = true;
  @Override public String categoryType() { return "PHONE"; }
}
