package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("TOOLS") @Getter @Setter @NoArgsConstructor
public class ToolsCategory extends Category {
  private Boolean safetyInfoRequired = true;
  @Override public String categoryType() { return "TOOLS"; }
}
