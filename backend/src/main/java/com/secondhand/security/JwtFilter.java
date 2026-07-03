package com.secondhand.security;

import com.secondhand.entity.Enums;
import com.secondhand.repository.UserRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.time.*;
import java.util.*;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtFilter extends OncePerRequestFilter {

  private final JwtService jwt;
  private final UserRepository users;

  public JwtFilter(JwtService jwt, UserRepository users) {
    this.jwt = jwt;
    this.users = users;
  }

  protected void doFilterInternal(
    HttpServletRequest req,
    HttpServletResponse res,
    FilterChain chain
  ) throws ServletException, IOException {
    String h = req.getHeader("Authorization");
    if (h != null && h.startsWith("Bearer ")) {
      try {
        var u = users
          .findByUsernameIgnoreCase(jwt.username(h.substring(7)))
          .orElseThrow();
        if (
          u.getStatus() == Enums.UserStatus.BLOCKED
        ) throw new RuntimeException();
        var auth = new UsernamePasswordAuthenticationToken(
          u,
          null,
          List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
      } catch (Exception e) {
        res.setStatus(401);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res
          .getWriter()
          .write(
            "{\"message\":\"Invalid, expired, or blocked authentication token\",\"status\":401,\"timestamp\":\"" +
              Instant.now() +
              "\",\"path\":\"" +
              req.getRequestURI() +
              "\"}"
          );
        return;
      }
    }
    chain.doFilter(req, res);
  }
}
