package com.secondhand.controller;

import com.secondhand.dto.ApiDtos.*;
import com.secondhand.entity.*;
import com.secondhand.exception.ApiException;
import com.secondhand.repository.*;
import com.secondhand.service.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

  private final AdvertisementRepository ads;
  private final UserRepository users;
  private final CategoryRepository cats;
  private final CityRepository cities;
  private final AdminReviewRepository reviews;
  private final MapperService mapper;
  private final CurrentUser current;

  public AdminController(
    AdvertisementRepository a,
    UserRepository u,
    CategoryRepository c,
    CityRepository ci,
    AdminReviewRepository r,
    MapperService m,
    CurrentUser cu
  ) {
    ads = a;
    users = u;
    cats = c;
    cities = ci;
    reviews = r;
    mapper = m;
    current = cu;
  }

  private Advertisement ad(Long id) {
    return ads
      .findById(id)
      .orElseThrow(() -> ApiException.notFound("Advertisement not found"));
  }

  @GetMapping("/ads/pending")
  @Transactional(readOnly = true)
  public List<AdDto> pending() {
    return ads
      .findByStatusOrderByCreatedAtDesc(Enums.AdStatus.PENDING)
      .stream()
      .map(mapper::ad)
      .toList();
  }

  @GetMapping("/ads/{id}")
  @Transactional(readOnly = true)
  public AdDto one(@PathVariable Long id) {
    return mapper.ad(ad(id));
  }

  @PutMapping("/ads/{id}/approve")
  @Transactional
  public AdDto approve(Authentication au, @PathVariable Long id) {
    Advertisement a = ad(id);
    if (a.getStatus() != Enums.AdStatus.PENDING) throw ApiException.bad(
      "Only pending ads can be approved"
    );
    a.setStatus(Enums.AdStatus.ACTIVE);
    review(a, current.get(au), Enums.ReviewDecision.APPROVED, null);
    return mapper.ad(a);
  }

  @PutMapping("/ads/{id}/reject")
  @Transactional
  public AdDto reject(
    Authentication au,
    @PathVariable Long id,
    @Valid @RequestBody RejectRequest r
  ) {
    Advertisement a = ad(id);
    if (
      a.getStatus() != Enums.AdStatus.PENDING &&
      a.getStatus() != Enums.AdStatus.ACTIVE
    ) throw ApiException.bad("Ad cannot be rejected");
    a.setStatus(Enums.AdStatus.REJECTED);
    a.setRejectionReason(r.reason());
    review(a, current.get(au), Enums.ReviewDecision.REJECTED, r.reason());
    return mapper.ad(a);
  }

  @DeleteMapping("/ads/{id}")
  @Transactional
  public void delete(Authentication au, @PathVariable Long id) {
    Advertisement a = ad(id);
    a.setStatus(Enums.AdStatus.DELETED);
    review(a, current.get(au), Enums.ReviewDecision.DELETED, null);
  }

  @GetMapping("/users")
  public List<UserDto> users() {
    return users.findAll().stream().map(mapper::user).toList();
  }

  @PutMapping("/users/{id}/block")
  @Transactional
  public UserDto block(Authentication au, @PathVariable Long id) {
    User u = user(id);
    if (current.get(au).getId().equals(id)) throw ApiException.bad(
      "Admins cannot block themselves"
    );
    u.setStatus(Enums.UserStatus.BLOCKED);
    return mapper.user(u);
  }

  @PutMapping("/users/{id}/unblock")
  @Transactional
  public UserDto unblock(@PathVariable Long id) {
    User u = user(id);
    u.setStatus(Enums.UserStatus.ACTIVE);
    return mapper.user(u);
  }

  @PostMapping("/categories")
  @Transactional
  public CategoryDto addCat(@Valid @RequestBody NameRequest r) {
    Category c = new Category();
    catApply(c, r);
    return mapper.category(cats.save(c));
  }

  @PutMapping("/categories/{id}")
  @Transactional
  public CategoryDto editCat(
    @PathVariable Long id,
    @Valid @RequestBody NameRequest r
  ) {
    Category c = cats
      .findById(id)
      .orElseThrow(() -> ApiException.notFound("Category not found"));
    catApply(c, r);
    return mapper.category(c);
  }

  @DeleteMapping("/categories/{id}")
  @Transactional
  public void delCat(@PathVariable Long id) {
    cats
      .findById(id)
      .orElseThrow(() -> ApiException.notFound("Category not found"))
      .setActive(false);
  }

  @PostMapping("/cities")
  @Transactional
  public CityDto addCity(@Valid @RequestBody NameRequest r) {
    City c = new City();
    cityApply(c, r);
    return mapper.city(cities.save(c));
  }

  @PutMapping("/cities/{id}")
  @Transactional
  public CityDto editCity(
    @PathVariable Long id,
    @Valid @RequestBody NameRequest r
  ) {
    City c = cities
      .findById(id)
      .orElseThrow(() -> ApiException.notFound("City not found"));
    cityApply(c, r);
    return mapper.city(c);
  }

  @DeleteMapping("/cities/{id}")
  @Transactional
  public void delCity(@PathVariable Long id) {
    cities
      .findById(id)
      .orElseThrow(() -> ApiException.notFound("City not found"))
      .setActive(false);
  }

  private User user(Long id) {
    return users
      .findById(id)
      .orElseThrow(() -> ApiException.notFound("User not found"));
  }

  private void review(
    Advertisement a,
    User u,
    Enums.ReviewDecision d,
    String n
  ) {
    AdminReview r = new AdminReview();
    r.setAdvertisement(a);
    r.setAdmin(u);
    r.setDecision(d);
    r.setNote(n);
    reviews.save(r);
  }

  private void catApply(Category c, NameRequest r) {
    c.setName(r.name());
    c.setActive(r.active() == null || r.active());
    c.setParent(
      r.parentId() == null
        ? null
        : cats
            .findById(r.parentId())
            .orElseThrow(() -> ApiException.bad("Invalid parent category"))
    );
  }

  private void cityApply(City c, NameRequest r) {
    c.setName(r.name());
    c.setProvince(r.province());
    c.setActive(r.active() == null || r.active());
  }
}
