package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("INDUSTRIAL_TOOL") @Getter @Setter @NoArgsConstructor
public class IndustrialToolCategory extends ToolsCategory {
  private Boolean powerSupported = true;
  @Override public String categoryType() { return "INDUSTRIAL_TOOL"; }
}
