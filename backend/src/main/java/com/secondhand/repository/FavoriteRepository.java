package com.secondhand.repository;

import com.secondhand.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
  List<Favorite> findByUserOrderByCreatedAtDesc(User user);
  Optional<Favorite> findByUserAndAdvertisement(User u, Advertisement a);
}
