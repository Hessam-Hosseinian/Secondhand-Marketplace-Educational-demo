package com.secondhand.entity;

public final class Enums {

  private Enums() {}

  public enum Role {
    USER,
    ADMIN,
  }

  public enum UserStatus {
    ACTIVE,
    BLOCKED,
  }

  public enum ItemCondition {
    NEW,
    LIKE_NEW,
    GOOD,
    FAIR,
    DAMAGED,
  }

  public enum AdStatus {
    PENDING,
    ACTIVE,
    REJECTED,
    DELETED,
    SOLD,
  }

  public enum ReviewDecision {
    APPROVED,
    REJECTED,
    DELETED,
  }
}
