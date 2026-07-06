package com.secondhand.service;

import com.secondhand.dto.ApiDtos.*;
import com.secondhand.entity.*;
import com.secondhand.entity.account.CustomerUser;
import com.secondhand.exception.ApiException;
import com.secondhand.repository.UserRepository;
import com.secondhand.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final JwtService jwt;

  public AuthService(UserRepository u, PasswordEncoder e, JwtService j) {
    users = u;
    encoder = e;
    jwt = j;
  }

  @Transactional
  public AuthResponse register(RegisterRequest r) {
    if (
      users.existsByUsernameIgnoreCase(r.username())
    ) throw ApiException.conflict("Username already exists");
    if (users.existsByPhoneNumber(r.phoneNumber())) throw ApiException.conflict(
      "Phone number already exists"
    );
    if (users.existsByEmailIgnoreCase(r.email())) throw ApiException.conflict(
      "Email already exists"
    );
    User u = new CustomerUser();
    u.setFullName(r.fullName().trim());
    u.setUsername(r.username().trim());
    u.setPasswordHash(encoder.encode(r.password()));
    u.setPhoneNumber(r.phoneNumber());
    u.setEmail(r.email().trim().toLowerCase());
    return response(users.save(u));
  }

  public AuthResponse login(LoginRequest r) {
    User u = users
      .findByUsernameIgnoreCase(r.username())
      .orElseThrow(() -> ApiException.bad("Invalid username or password"));
    if (u.getStatus() == Enums.UserStatus.BLOCKED) throw ApiException.forbidden(
      "This account is blocked"
    );
    if (
      !encoder.matches(r.password(), u.getPasswordHash())
    ) throw ApiException.bad("Invalid username or password");
    return response(u);
  }

  private AuthResponse response(User u) {
    return new AuthResponse(
      jwt.generate(u),
      u.getId(),
      u.getFullName(),
      u.getUsername(),
      u.getRole().name(),
      u.getStatus().name()
    );
  }
}
