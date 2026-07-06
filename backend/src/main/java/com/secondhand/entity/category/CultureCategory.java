package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("CULTURE") @Getter @Setter @NoArgsConstructor
public class CultureCategory extends Category {
  private Boolean languageSupported = true;
  @Override public String categoryType() { return "CULTURE"; }
}
