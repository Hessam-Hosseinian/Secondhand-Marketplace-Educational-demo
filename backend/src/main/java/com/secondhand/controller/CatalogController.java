package com.secondhand.controller;

import com.secondhand.dto.ApiDtos.*;
import com.secondhand.repository.*;
import com.secondhand.service.MapperService;
import java.util.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CatalogController {

  private final CategoryRepository categories;
  private final CityRepository cities;
  private final MapperService mapper;

  public CatalogController(
    CategoryRepository c,
    CityRepository ci,
    MapperService m
  ) {
    categories = c;
    cities = ci;
    mapper = m;
  }

  @GetMapping("/categories")
  @Transactional(readOnly = true)
  public List<CategoryDto> categories() {
    return categories
      .findAllByOrderByName()
      .stream()
      .filter(x -> x.isActive())
      .map(mapper::category)
      .toList();
  }

  @GetMapping("/cities")
  public List<CityDto> cities() {
    return cities
      .findAllByOrderByName()
      .stream()
      .filter(x -> x.isActive())
      .map(mapper::city)
      .toList();
  }
}
