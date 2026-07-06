package com.secondhand.client.auth;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.prefs.Preferences;

public final class SessionManager {

  private static String token, username, fullName, role, status;
  private static long userId;
  private static final Preferences STORE = Preferences.userNodeForPackage(
    SessionManager.class
  );

  static {
    token = STORE.get("token", null);
    username = STORE.get("username", null);
    fullName = STORE.get("fullName", null);
    role = STORE.get("role", null);
    status = STORE.get("status", null);
    userId = STORE.getLong("userId", 0);
  }

  public static void login(JsonNode n) {
    token = n.path("token").asText();
    userId = n.path("userId").asLong();
    username = n.path("username").asText();
    fullName = n.path("fullName").asText();
    role = n.path("role").asText();
    status = n.path("status").asText();
    STORE.put("token", token);
    STORE.putLong("userId", userId);
    STORE.put("username", username);
    STORE.put("fullName", fullName);
    STORE.put("role", role);
    STORE.put("status", status);
  }

  public static void clear() {
    token = username = fullName = role = status = null;
    userId = 0;
    try {
      STORE.clear();
    } catch (Exception ignored) {}
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
