package com.secondhand.entity.product;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("GAME_CONSOLE") @Getter @Setter @NoArgsConstructor
public class GameConsoleAdvertisement extends Advertisement {
  private String manufacturer;
  private String storage;
  private Integer controllerCount;
  private String region;
  @Override public String productType() { return "GAME_CONSOLE"; }
}
