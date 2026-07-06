package com.secondhand.controller;

import com.secondhand.dto.ApiDtos.*;
import com.secondhand.service.CatalogService;
import java.util.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CatalogController {

  private final CatalogService catalog;

  public CatalogController(CatalogService catalog) {
    this.catalog = catalog;
  }

  @GetMapping("/categories")
  public List<CategoryDto> categories() {
    return catalog.categories();
  }

  @GetMapping("/cities")
  public List<CityDto> cities() {
    return catalog.cities();
  }

  @GetMapping("/categories/{id}/attributes")
  public List<CategoryAttributeDto> attributes(@PathVariable Long id) {
    return catalog.attributes(id);
  }
}
