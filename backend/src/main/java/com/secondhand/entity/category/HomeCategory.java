package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("HOME") @Getter @Setter @NoArgsConstructor
public class HomeCategory extends Category {
  private Boolean dimensionsSupported = true;
  @Override public String categoryType() { return "HOME"; }
}
