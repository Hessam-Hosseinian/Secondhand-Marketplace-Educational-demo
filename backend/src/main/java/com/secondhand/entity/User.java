package com.secondhand.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

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

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Enums.Role role = Enums.Role.USER;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Enums.UserStatus status = Enums.UserStatus.ACTIVE;

  @Column(nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();
}
