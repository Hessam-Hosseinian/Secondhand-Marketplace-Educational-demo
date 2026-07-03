package com.secondhand.controller;

import com.secondhand.dto.ApiDtos.*;
import com.secondhand.service.AuthService;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService service;

  public AuthController(AuthService s) {
    service = s;
  }

  @PostMapping("/register")
  ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest r) {
    return ResponseEntity.status(201).body(service.register(r));
  }

  @PostMapping("/login")
  AuthResponse login(@Valid @RequestBody LoginRequest r) {
    return service.login(r);
  }

  @PostMapping("/logout")
  Map<String, String> logout() {
    return Map.of("message", "Logged out");
  }
}
