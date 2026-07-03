package com.secondhand.controller;

import com.secondhand.dto.ApiDtos.*;
import com.secondhand.entity.User;
import com.secondhand.service.*;
import jakarta.validation.Valid;
import java.math.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AdController {

  private final AdService ads;
  private final CurrentUser current;

  public AdController(AdService a, CurrentUser c) {
    ads = a;
    current = c;
  }

  @GetMapping("/ads")
  List<AdDto> search(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) Long categoryId,
    @RequestParam(required = false) Long cityId,
    @RequestParam(required = false) BigDecimal minPrice,
    @RequestParam(required = false) BigDecimal maxPrice,
    @RequestParam(required = false) String condition,
    @RequestParam(defaultValue = "newest") String sort
  ) {
    return ads.search(
      keyword,
      categoryId,
      cityId,
      minPrice,
      maxPrice,
      condition,
      sort
    );
  }

  @GetMapping("/ads/{id}")
  AdDto one(@PathVariable Long id) {
    return ads.publicOne(id);
  }

  @PostMapping("/ads")
  ResponseEntity<AdDto> create(
    Authentication a,
    @Valid @RequestBody AdRequest r
  ) {
    return ResponseEntity.status(201).body(ads.create(current.get(a), r));
  }

  @PutMapping("/ads/{id}")
  AdDto edit(
    Authentication a,
    @PathVariable Long id,
    @Valid @RequestBody AdRequest r
  ) {
    return ads.edit(current.get(a), id, r);
  }

  @DeleteMapping("/ads/{id}")
  ResponseEntity<Void> delete(Authentication a, @PathVariable Long id) {
    ads.delete(current.get(a), id);
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/ads/{id}/sold")
  AdDto sold(Authentication a, @PathVariable Long id) {
    return ads.sold(current.get(a), id);
  }

  @GetMapping("/my/ads")
  List<AdDto> mine(Authentication a) {
    return ads.mine(current.get(a));
  }
}
