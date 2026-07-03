package com.secondhand.repository;

import com.secondhand.entity.City;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CityRepository extends JpaRepository<City, Long> {
  List<City> findAllByOrderByName();
  Optional<City> findByName(String name);
}
