package com.secondhand.entity.account;
import com.secondhand.entity.*;

import jakarta.persistence.*;
import lombok.*;

@Entity
@DiscriminatorValue("ADMIN")
@Getter
@Setter
@NoArgsConstructor
public class AdminUser extends User {
  private String department = "MARKETPLACE";
  private Integer permissionLevel = 10;

  @Override
  public String accountType() {
    return "ADMIN";
  }
}
