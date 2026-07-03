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

  public AdService(
    AdvertisementRepository a,
    CategoryRepository c,
    CityRepository ci,
    MapperService m
  ) {
    ads = a;
    cats = c;
    cities = ci;
    mapper = m;
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
    Advertisement a = new Advertisement();
    apply(a, r);
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
    apply(a, r);
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

  private void apply(Advertisement a, AdRequest r) {
    Category c = cats
      .findById(r.categoryId())
      .filter(Category::isActive)
      .orElseThrow(() -> ApiException.bad("Invalid category"));
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
    if (r.imageUrl() != null && !r.imageUrl().isBlank()) {
      a.getImages().clear();
      AdvertisementImage i = new AdvertisementImage();
      i.setAdvertisement(a);
      i.setImageUrl(r.imageUrl().trim());
      a.getImages().add(i);
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
