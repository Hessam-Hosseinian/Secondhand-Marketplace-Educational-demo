package com.secondhand.service;

import com.secondhand.dto.ApiDtos.*;
import com.secondhand.entity.*;
import com.secondhand.exception.ApiException;
import com.secondhand.repository.*;
import java.math.*;
import java.util.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdService {

  private final AdvertisementRepository ads;
  private final CategoryRepository cats;
  private final CityRepository cities;
  private final MapperService mapper;
  private final CategoryAttributeRepository categoryAttributes;
  private final ProductFactory products;

  public AdService(
    AdvertisementRepository a,
    CategoryRepository c,
    CityRepository ci,
    MapperService m,
    CategoryAttributeRepository ca,
    ProductFactory products
  ) {
    ads = a;
    cats = c;
    cities = ci;
    mapper = m;
    categoryAttributes = ca;
    this.products = products;
  }

  @Transactional(readOnly = true)
  public List<AdDto> search(
    String k,
    Long cat,
    Long city,
    BigDecimal min,
    BigDecimal max,
    String cond,
    String sort
  ) {
    if (min != null && min.signum() < 0) throw ApiException.bad(
      "Minimum price cannot be negative"
    );
    if (max != null && max.signum() < 0) throw ApiException.bad(
      "Maximum price cannot be negative"
    );
    if (
      min != null && max != null && min.compareTo(max) > 0
    ) throw ApiException.bad("Minimum price cannot exceed maximum price");
    Enums.ItemCondition condition =
      cond == null || cond.isBlank() ? null : parseCondition(cond);
    Sort s = "cheapest".equals(sort)
      ? Sort.by("price").ascending()
      : "expensive".equals(sort)
        ? Sort.by("price").descending()
        : Sort.by("createdAt").descending();
    return ads
      .search(blank(k), cat, city, min, max, condition, s)
      .stream()
      .map(mapper::ad)
      .toList();
  }

  @Transactional(readOnly = true)
  public AdDto publicOne(Long id) {
    Advertisement a = get(id);
    if (a.getStatus() != Enums.AdStatus.ACTIVE) throw ApiException.notFound(
      "Advertisement not found"
    );
    return mapper.ad(a);
  }

  @Transactional
  public AdDto create(User owner, AdRequest r) {
    Category category = activeCategory(r.categoryId());
    Advertisement a = products.create(category, r.attributes());
    apply(a, r, category);
    a.setOwner(owner);
    a.setStatus(Enums.AdStatus.PENDING);
    return mapper.ad(ads.save(a));
  }

  @Transactional
  public AdDto edit(User u, Long id, AdRequest r) {
    Advertisement a = owned(u, id);
    if (
      a.getStatus() == Enums.AdStatus.DELETED ||
      a.getStatus() == Enums.AdStatus.SOLD
    ) throw ApiException.bad("This advertisement cannot be edited");
    Category category = activeCategory(r.categoryId());
    if (
      !a.getCategory().getId().equals(category.getId())
    ) throw ApiException.bad(
      "The category of an existing advertisement cannot be changed"
    );
    apply(a, r, category);
    a.setStatus(Enums.AdStatus.PENDING);
    a.setRejectionReason(null);
    return mapper.ad(a);
  }

  @Transactional
  public void delete(User u, Long id) {
    Advertisement a = owned(u, id);
    a.setStatus(Enums.AdStatus.DELETED);
  }

  @Transactional
  public AdDto sold(User u, Long id) {
    Advertisement a = owned(u, id);
    if (a.getStatus() != Enums.AdStatus.ACTIVE) throw ApiException.bad(
      "Only an active advertisement can be marked sold"
    );
    a.setStatus(Enums.AdStatus.SOLD);
    return mapper.ad(a);
  }

  @Transactional(readOnly = true)
  public List<AdDto> mine(User u) {
    return ads
      .findByOwnerOrderByCreatedAtDesc(u)
      .stream()
      .map(mapper::ad)
      .toList();
  }

  Advertisement get(Long id) {
    return ads
      .findById(id)
      .orElseThrow(() -> ApiException.notFound("Advertisement not found"));
  }

  private Advertisement owned(User u, Long id) {
    Advertisement a = get(id);
    if (!a.getOwner().getId().equals(u.getId())) throw ApiException.forbidden(
      "You do not own this advertisement"
    );
    return a;
  }

  private void apply(Advertisement a, AdRequest r, Category c) {
    City city = cities
      .findById(r.cityId())
      .filter(City::isActive)
      .orElseThrow(() -> ApiException.bad("Invalid city"));
    a.setTitle(r.title().trim());
    a.setDescription(r.description().trim());
    a.setPrice(r.price());
    a.setItemCondition(parseCondition(r.itemCondition()));
    a.setCategory(c);
    a.setCity(city);
    a.setAttributesText(r.attributesText());
    applyAttributes(a, c, r.attributes());
    if (r.imageUrls() != null) {
      List<String> imageUrls = r.imageUrls();
      a.getImages().clear();
      for (int order = 0; order < imageUrls.size(); order++) {
        AdvertisementImage image = new AdvertisementImage();
        image.setAdvertisement(a);
        image.setImageUrl(imageUrls.get(order).trim());
        image.setSortOrder(order);
        a.getImages().add(image);
      }
    } else if (r.imageUrl() != null && !r.imageUrl().isBlank()) {
      a.getImages().clear();
      AdvertisementImage image = new AdvertisementImage();
      image.setAdvertisement(a);
      image.setImageUrl(r.imageUrl().trim());
      image.setSortOrder(0);
      a.getImages().add(image);
    }
  }

  private Category activeCategory(Long id) {
    return cats
      .findById(id)
      .filter(Category::isActive)
      .orElseThrow(() -> ApiException.bad("Invalid category"));
  }

  private void applyAttributes(
    Advertisement ad,
    Category category,
    Map<String, String> values
  ) {
    Map<String, String> safeValues = values == null ? Map.of() : values;
    for (Map.Entry<String, String> entry : safeValues.entrySet()) {
      if (
        entry.getKey() == null || entry.getKey().length() > 255
      ) throw ApiException.bad("Invalid attribute key");
      if (
        entry.getValue() != null && entry.getValue().length() > 1000
      ) throw ApiException.bad(
        "Attribute values must be at most 1000 characters"
      );
    }
    List<CategoryAttribute> definitions =
      categoryAttributes.findByCategoryIdOrderBySortOrder(category.getId());
    for (CategoryAttribute definition : definitions) {
      String value = safeValues
        .getOrDefault(definition.getAttributeKey(), "")
        .trim();
      if (definition.isRequired() && value.isBlank()) throw ApiException.bad(
        "مقدار «" + definition.getLabel() + "» الزامی است"
      );
    }
    ad.getAttributes().clear();
    for (CategoryAttribute definition : definitions) {
      String value = safeValues
        .getOrDefault(definition.getAttributeKey(), "")
        .trim();
      if (value.isBlank()) continue;
      AdvertisementAttribute attribute = new AdvertisementAttribute();
      attribute.setAdvertisement(ad);
      attribute.setAttributeKey(definition.getAttributeKey());
      attribute.setLabel(definition.getLabel());
      attribute.setValue(value);
      ad.getAttributes().add(attribute);
    }
  }

  private Enums.ItemCondition parseCondition(String s) {
    try {
      return Enums.ItemCondition.valueOf(s.toUpperCase());
    } catch (Exception e) {
      throw ApiException.bad("Invalid item condition");
    }
  }

  private String blank(String s) {
    return s == null || s.isBlank() ? null : s.trim();
  }
}
