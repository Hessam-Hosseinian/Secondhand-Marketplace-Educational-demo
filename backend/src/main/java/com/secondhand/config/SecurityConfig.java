package com.secondhand.config;

import com.secondhand.security.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SecurityFilterChain filter(HttpSecurity h, JwtFilter jwt) throws Exception {
    return h
      .csrf(c -> c.disable())
      .cors(c -> {})
      .sessionManagement(s ->
        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authorizeHttpRequests(a ->
        a
          .requestMatchers("/api/auth/register", "/api/auth/login")
          .permitAll()
          .requestMatchers(
            HttpMethod.GET,
            "/api/ads",
            "/api/ads/*",
            "/api/categories",
            "/api/categories/*/attributes",
            "/api/cities",
            "/api/users/*/ratings",
            "/uploads/**"
          )
          .permitAll()
          .requestMatchers("/api/admin/**")
          .hasRole("ADMIN")
          .anyRequest()
          .authenticated()
      )
      .exceptionHandling(e ->
        e
          .authenticationEntryPoint((q, r, x) -> {
            r.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            r.setContentType("application/json");
            r.setCharacterEncoding(StandardCharsets.UTF_8.name());
            r.getWriter().write(
              "{\"message\":\"Authentication required\",\"status\":401}"
            );
          })
          .accessDeniedHandler((q, r, x) -> {
            r.setStatus(403);
            r.setContentType("application/json");
            r.setCharacterEncoding(StandardCharsets.UTF_8.name());
            r.getWriter().write(
              "{\"message\":\"Access denied\",\"status\":403}"
            );
          })
      )
      .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
      .build();
  }
}
