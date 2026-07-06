package com.secondhand.repository;

import com.secondhand.entity.User;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsernameIgnoreCase(String username);
  boolean existsByUsernameIgnoreCase(String username);
  boolean existsByPhoneNumber(String phoneNumber);
  boolean existsByEmailIgnoreCase(String email);
}
