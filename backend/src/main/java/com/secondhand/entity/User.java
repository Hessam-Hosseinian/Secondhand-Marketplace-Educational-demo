package com.secondhand.entity;

import com.secondhand.entity.base.BaseAccount;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DiscriminatorFormula;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("role")
@Getter
@Setter
@NoArgsConstructor
public abstract class User extends BaseAccount {
  private String avatarUrl;
  private String bio;
}
