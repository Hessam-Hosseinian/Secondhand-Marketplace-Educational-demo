package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("BOOK") @Getter @Setter @NoArgsConstructor
public class BookCategory extends CultureCategory {
  private Boolean authorRequired = true;
  @Override public String categoryType() { return "BOOK"; }
}
