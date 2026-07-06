package com.secondhand.entity.account;
import com.secondhand.entity.*;

import jakarta.persistence.*;
import lombok.*;

@Entity
@DiscriminatorValue("USER")
@Getter
@Setter
@NoArgsConstructor
public class CustomerUser extends User {
  private Integer successfulDeals;
  private Boolean verifiedSeller;

  @Override
  public String accountType() {
    return "CUSTOMER";
  }
}
