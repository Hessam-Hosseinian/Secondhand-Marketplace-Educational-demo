package com.secondhand.dto;

import jakarta.validation.constraints.*;
import java.math.*;
import java.time.*;
import java.util.*;

public final class ApiDtos {

  private ApiDtos() {}

  public record RegisterRequest(
    @NotBlank @Size(max = 255) String fullName,
    @NotBlank @Size(max = 255) String username,
    @NotBlank @Size(min = 6, max = 72) String password,
    @NotBlank @Size(max = 255) String phoneNumber,
    @NotBlank @Email @Size(max = 255) String email
  ) {}

  public record LoginRequest(
    @NotBlank @Size(max = 255) String username,
    @NotBlank @Size(max = 72) String password
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
    @NotBlank @Size(max = 255) String title,
    @NotBlank @Size(max = 5000) String description,
    @NotNull @DecimalMin("0") BigDecimal price,
    @NotBlank String itemCondition,
    @NotNull Long categoryId,
    @NotNull Long cityId,
    @Size(max = 3000) String attributesText,
    Map<String, String> attributes,
    @Size(max = 1000) String imageUrl,
    @Size(max = 10) List<@NotBlank @Size(max = 1000) String> imageUrls
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

  public record MessageRequest(@NotBlank @Size(max = 2000) String content) {}

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
    @Size(max = 255) String comment
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
    @NotBlank @Size(max = 255) String name,
    @Size(max = 255) String province,
    Long parentId,
    Boolean active
  ) {}

  public record RejectRequest(@NotBlank @Size(max = 255) String reason) {}
}
