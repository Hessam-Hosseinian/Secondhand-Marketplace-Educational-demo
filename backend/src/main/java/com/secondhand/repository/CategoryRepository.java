package com.secondhand.repository;

import com.secondhand.entity.Category;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
  List<Category> findAllByOrderByName();
  Optional<Category> findByName(String name);
}
