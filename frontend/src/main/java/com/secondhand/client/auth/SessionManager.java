package com.secondhand.client.auth;

import com.fasterxml.jackson.databind.JsonNode;

public final class SessionManager {

  private static String token, username, fullName, role, status;
  private static long userId;

  public static void login(JsonNode n) {
    token = n.path("token").asText();
    userId = n.path("userId").asLong();
    username = n.path("username").asText();
    fullName = n.path("fullName").asText();
    role = n.path("role").asText();
    status = n.path("status").asText();
  }

  public static void clear() {
    token = username = fullName = role = status = null;
    userId = 0;
  }

  public static boolean loggedIn() {
    return token != null;
  }

  public static boolean admin() {
    return "ADMIN".equals(role);
  }

  public static String token() {
    return token;
  }

  public static long userId() {
    return userId;
  }

  public static String username() {
    return username;
  }

  public static String fullName() {
    return fullName;
  }
}
