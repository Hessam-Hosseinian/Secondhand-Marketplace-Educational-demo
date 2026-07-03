package com.secondhand.service;

import com.secondhand.dto.ApiDtos.*;
import com.secondhand.entity.*;
import com.secondhand.repository.SellerRatingRepository;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class MapperService {

  private final SellerRatingRepository ratings;

  public MapperService(SellerRatingRepository r) {
    ratings = r;
  }

  public CategoryDto category(Category c) {
    return new CategoryDto(
      c.getId(),
      c.getName(),
      c.getParent() == null ? null : c.getParent().getId(),
      c.getParent() == null ? null : c.getParent().getName(),
      c.isActive()
    );
  }

  public CityDto city(City c) {
    return new CityDto(c.getId(), c.getName(), c.getProvince(), c.isActive());
  }

  public AdDto ad(Advertisement a) {
    return new AdDto(
      a.getId(),
      a.getTitle(),
      a.getDescription(),
      a.getPrice(),
      a.getItemCondition().name(),
      a.getStatus().name(),
      a.getOwner().getId(),
      a.getOwner().getFullName(),
      a.getCategory().getId(),
      a.getCategory().getName(),
      a.getCategory().getParent() == null
        ? a.getCategory().getName()
        : a.getCategory().getParent().getName(),
      a.getCity().getId(),
      a.getCity().getName(),
      a.getAttributesText(),
      a.getRejectionReason(),
      a.getImages().stream().map(AdvertisementImage::getImageUrl).toList(),
      ratings.average(a.getOwner()),
      a.getCreatedAt(),
      a.getUpdatedAt()
    );
  }

  public UserDto user(User u) {
    return new UserDto(
      u.getId(),
      u.getFullName(),
      u.getUsername(),
      u.getPhoneNumber(),
      u.getRole().name(),
      u.getStatus().name(),
      u.getCreatedAt()
    );
  }
}
