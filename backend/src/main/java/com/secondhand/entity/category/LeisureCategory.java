package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("LEISURE") @Getter @Setter @NoArgsConstructor
public class LeisureCategory extends Category {
  private Boolean ageGroupSupported = true;
  @Override public String categoryType() { return "LEISURE"; }
}
