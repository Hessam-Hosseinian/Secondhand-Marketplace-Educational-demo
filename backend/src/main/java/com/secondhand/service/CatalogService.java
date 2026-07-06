package com.secondhand.service;

import com.secondhand.dto.ApiDtos.*;
import com.secondhand.entity.CategoryAttribute;
import com.secondhand.repository.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogService {
  private final CategoryRepository categories;
  private final CityRepository cities;
  private final CategoryAttributeRepository attributes;
  private final MapperService mapper;

  public CatalogService(
    CategoryRepository categories,
    CityRepository cities,
    CategoryAttributeRepository attributes,
    MapperService mapper
  ) {
    this.categories = categories;
    this.cities = cities;
    this.attributes = attributes;
    this.mapper = mapper;
  }

  @Transactional(readOnly = true)
  public List<CategoryDto> categories() {
    return categories.findAllByOrderByName().stream()
      .filter(x -> x.isActive())
      .map(mapper::category)
      .toList();
  }

  @Transactional(readOnly = true)
  public List<CityDto> cities() {
    return cities.findAllByOrderByName().stream()
      .filter(x -> x.isActive())
      .map(mapper::city)
      .toList();
  }

  @Transactional(readOnly = true)
  public List<CategoryAttributeDto> attributes(Long categoryId) {
    return attributes.findByCategoryIdOrderBySortOrder(categoryId).stream()
      .map(this::attribute)
      .toList();
  }

  private CategoryAttributeDto attribute(CategoryAttribute a) {
    List<String> options = a.getOptionsText() == null || a.getOptionsText().isBlank()
      ? List.of()
      : Arrays.stream(a.getOptionsText().split("\\|")).map(String::trim).toList();
    return new CategoryAttributeDto(
      a.getId(), a.getAttributeKey(), a.getLabel(), a.getInputType(),
      options, a.isRequired(), a.getSortOrder()
    );
  }
}
