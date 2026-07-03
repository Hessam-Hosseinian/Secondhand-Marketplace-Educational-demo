package com.secondhand.client.model;

public record Option(long id, String name, Long parentId) {
  @Override
  public String toString() {
    return name;
  }
}
