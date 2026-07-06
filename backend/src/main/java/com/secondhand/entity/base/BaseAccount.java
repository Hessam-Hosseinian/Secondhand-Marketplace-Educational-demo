package com.secondhand.entity.base;

import com.secondhand.entity.Enums;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseAccount {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String fullName;

  @Column(nullable = false, unique = true)
  private String username;

  @Column(nullable = false)
  private String passwordHash;

  private String phoneNumber;

  @Column
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Enums.Role role = Enums.Role.USER;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Enums.UserStatus status = Enums.UserStatus.ACTIVE;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  public abstract String accountType();
}
