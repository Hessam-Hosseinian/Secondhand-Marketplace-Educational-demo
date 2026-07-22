package com.secondhand.client.api;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.secondhand.client.auth.SessionManager;
import java.io.File;
import java.net.*;
import java.net.http.*;
import java.nio.file.Files;
import java.time.Duration;

public class ApiClient {

  public static final ObjectMapper JSON = new ObjectMapper().registerModule(
    new JavaTimeModule()
  );
  private final HttpClient client = HttpClient.newBuilder()
    .connectTimeout(Duration.ofSeconds(5))
    .followRedirects(HttpClient.Redirect.NORMAL)
    .build();

  public JsonNode get(String path) {
    return send("GET", path, null);
  }

  public JsonNode post(String path, Object body) {
    return send("POST", path, body);
  }

  public JsonNode put(String path, Object body) {
    return send("PUT", path, body);
  }

  public JsonNode delete(String path) {
    return send("DELETE", path, null);
  }

  public JsonNode uploadImage(File file) {
    try {
      String boundary = "----Secondhand" + System.currentTimeMillis();
      String type = Files.probeContentType(file.toPath());
      String head =
        "--" +
        boundary +
        "\r\nContent-Disposition: form-data; name=\"file\"; filename=\"" +
        file.getName().replace("\"", "") +
        "\"\r\nContent-Type: " +
        type +
        "\r\n\r\n";
      String tail = "\r\n--" + boundary + "--\r\n";
      HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.concat(
        HttpRequest.BodyPublishers.ofString(head),
        HttpRequest.BodyPublishers.ofFile(file.toPath()),
        HttpRequest.BodyPublishers.ofString(tail)
      );
      if (!SessionManager.loggedIn()) throw new ApiException(
        401,
        "Please log in before uploading an image."
      );
      HttpRequest request = HttpRequest.newBuilder(
        URI.create(ApiConfig.BASE_URL + "/api/images/upload")
      )
        .timeout(Duration.ofSeconds(30))
        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
        .header("Authorization", "Bearer " + SessionManager.token())
        .POST(body)
        .build();
      HttpResponse<String> response = client.send(
        request,
        HttpResponse.BodyHandlers.ofString()
      );
      if (response.statusCode() == 401) {
        SessionManager.clear();
        throw new ApiException(
          401,
          "Your session expired. Please log in again."
        );
      }
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        String message;
        try {
          message = JSON.readTree(response.body())
            .path("message")
            .asText("Image upload failed");
        } catch (Exception ignored) {
          message = "Image upload failed";
        }
        throw new ApiException(response.statusCode(), message);
      }
      return JSON.readTree(response.body());
    } catch (ApiException e) {
      throw e;
    } catch (Exception e) {
      throw new ApiException(0, "Could not upload the selected image");
    }
  }

  private JsonNode send(String method, String path, Object body) {
    try {
      HttpRequest.Builder b = HttpRequest.newBuilder(
        URI.create(ApiConfig.BASE_URL + path)
      )
        .timeout(Duration.ofSeconds(15))
        .header("Accept", "application/json");
      if (SessionManager.loggedIn()) b.header(
        "Authorization",
        "Bearer " + SessionManager.token()
      );
      if (body != null) b.header("Content-Type", "application/json").method(
        method,
        HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(body))
      );
      else b.method(method, HttpRequest.BodyPublishers.noBody());
      HttpResponse<String> r = client.send(
        b.build(),
        HttpResponse.BodyHandlers.ofString()
      );
      if (r.statusCode() == 401) {
        SessionManager.clear();
        throw new ApiException(
          401,
          "Your session expired. Please log in again."
        );
      }
      if (r.statusCode() < 200 || r.statusCode() >= 300) {
        String message;
        try {
          message = JSON.readTree(r.body())
            .path("message")
            .asText("Request failed");
        } catch (Exception x) {
          message = "Request failed (" + r.statusCode() + ")";
        }
        throw new ApiException(r.statusCode(), message);
      }
      return r.body().isBlank()
        ? JSON.createObjectNode()
        : JSON.readTree(r.body());
    } catch (ApiException e) {
      throw e;
    } catch (Exception e) {
      throw new ApiException(
        0,
        "Cannot connect to backend at " + ApiConfig.BASE_URL
      );
    }
  }

  public static class ApiException extends RuntimeException {

    private final int statusCode;

    public ApiException(int statusCode, String m) {
      super(m);
      this.statusCode = statusCode;
    }

    public int statusCode() {
      return statusCode;
    }
  }
}
