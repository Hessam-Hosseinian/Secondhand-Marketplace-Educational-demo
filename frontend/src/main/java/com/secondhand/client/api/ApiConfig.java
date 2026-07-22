package com.secondhand.client.api;

public final class ApiConfig {

  public static final String BASE_URL = baseUrl();

  private ApiConfig() {}

  private static String baseUrl() {
    String configured = System.getProperty("secondhand.api.url");
    if (configured == null || configured.isBlank()) configured = System.getenv(
      "SECONDHAND_API_URL"
    );
    if (configured == null || configured.isBlank()) configured =
      "http://localhost:8080";
    return configured.replaceAll("/+$", "");
  }
}
