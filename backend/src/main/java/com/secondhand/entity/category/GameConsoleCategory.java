package com.secondhand.entity.category;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("GAME_CONSOLE") @Getter @Setter @NoArgsConstructor
public class GameConsoleCategory extends LeisureCategory {
  private Boolean storageSupported = true;
  @Override public String categoryType() { return "GAME_CONSOLE"; }
}
