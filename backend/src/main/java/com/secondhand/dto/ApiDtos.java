package com.secondhand.dto;

import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

public final class ApiDtos {

  private ApiDtos() {}

  public record RegisterRequest(
    @NotBlank String fullName,
    @NotBlank String username,
    @Size(min = 6) String password,
    @NotBlank String phoneNumber,
    @NotBlank @Email String email
  ) {}

  public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password
  ) {}

  public record AuthResponse(
    String token,
    Long userId,
    String fullName,
    String username,
    String role,
    String status
  ) {}

  public record CategoryDto(
    Long id,
    String name,
    Long parentId,
    String parentName,
    boolean active,
    String categoryType
  ) {}

  public record CityDto(
    Long id,
    String name,
    String province,
    boolean active
  ) {}

  public record CategoryAttributeDto(
    Long id,
    String key,
    String label,
    String inputType,
    List<String> options,
    boolean required,
    int sortOrder
  ) {}

  public record AdRequest(
    @NotBlank String title,
    @NotBlank String description,
    @NotNull @DecimalMin("0") BigDecimal price,
    @NotBlank String itemCondition,
    @NotNull Long categoryId,
    @NotNull Long cityId,
    String attributesText,
    Map<String, String> attributes,
    String imageUrl,
    List<String> imageUrls
  ) {}

  public record AdDto(
    Long id,
    String productType,
    String title,
    String description,
    BigDecimal price,
    String itemCondition,
    String status,
    Long ownerId,
    String ownerName,
    Long categoryId,
    String categoryName,
    String mainCategoryName,
    Long cityId,
    String cityName,
    String attributesText,
    Map<String, String> attributes,
    String rejectionReason,
    List<String> imageUrls,
    Double sellerAverageRating,
    long sellerRatingCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
  ) {}

  public record MessageRequest(@NotBlank String content) {}

  public record MessageDto(
    Long id,
    Long senderId,
    String senderName,
    String content,
    LocalDateTime createdAt
  ) {}

  public record ConversationDto(
    Long id,
    Long advertisementId,
    String advertisementTitle,
    Long buyerId,
    String buyerName,
    Long sellerId,
    String sellerName,
    String lastMessage,
    LocalDateTime lastMessageAt,
    LocalDateTime createdAt
  ) {}

  public record RatingRequest(
    @NotNull Long advertisementId,
    @NotNull Long sellerId,
    @Min(1) @Max(5) int rating,
    String comment
  ) {}

  public record RatingDto(
    Long id,
    Long buyerId,
    String buyerName,
    int rating,
    String comment,
    LocalDateTime createdAt
  ) {}

  public record UserDto(
    Long id,
    String fullName,
    String username,
    String phoneNumber,
    String email,
    String role,
    String status,
    String accountType,
    LocalDateTime createdAt
  ) {}

  public record NameRequest(
    @NotBlank String name,
    String province,
    Long parentId,
    Boolean active
  ) {}

  public record RejectRequest(@NotBlank String reason) {}
}
