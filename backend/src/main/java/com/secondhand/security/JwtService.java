package com.secondhand.security;

import com.secondhand.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.*;
import java.util.*;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey key;
  private final long expiration;

  public JwtService(
    @Value("${app.jwt.secret}") String secret,
    @Value("${app.jwt.expiration}") long expiration
  ) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.expiration = expiration;
  }

  public String generate(User u) {
    return Jwts.builder()
      .subject(u.getUsername())
      .claim("role", u.getRole().name())
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + expiration))
      .signWith(key)
      .compact();
  }

  public String username(String token) {
    return Jwts.parser()
      .verifyWith(key)
      .build()
      .parseSignedClaims(token)
      .getPayload()
      .getSubject();
  }
}
