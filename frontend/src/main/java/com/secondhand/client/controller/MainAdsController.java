package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.auth.SessionManager;
import com.secondhand.client.model.Option;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class MainAdsController extends BaseController {

  @FXML
  private TextField keywordField, minPriceField, maxPriceField;

  @FXML
  private ComboBox<Option> categoryBox, cityBox;

  @FXML
  private ComboBox<String> conditionBox, sortBox;

  @FXML
  private TilePane adsPane;

  @FXML
  private Button adminButton, loginButton, logoutButton, myAdsButton,
    favoritesButton, messagesButton;

  @FXML
  private Label userLabel, resultLabel;

  private final List<Option> categories = new ArrayList<>(),
    cities = new ArrayList<>();

  @FXML
  private void initialize() {
    safe(() -> {
      boolean signedIn = SessionManager.loggedIn();
      userLabel.setText(signedIn ? SessionManager.fullName() : "");
      userLabel.setVisible(signedIn);
      userLabel.setManaged(signedIn);
      loginButton.setVisible(!signedIn);
      loginButton.setManaged(!signedIn);
      logoutButton.setVisible(signedIn);
      logoutButton.setManaged(signedIn);
      myAdsButton.setVisible(signedIn);
      myAdsButton.setManaged(signedIn);
      favoritesButton.setVisible(signedIn);
      favoritesButton.setManaged(signedIn);
      messagesButton.setVisible(signedIn);
      messagesButton.setManaged(signedIn);
      adminButton.setVisible(SessionManager.admin());
      adminButton.setManaged(SessionManager.admin());
      loadOptions();
      search();
    });
  }

  private void loadOptions() {
    categories.clear();
    cities.clear();
    for (JsonNode n : api.get("/api/categories"))
      categories.add(
        new Option(
          n.path("id").asLong(),
          n.path("name").asText(),
          n.path("parentId").isNull() ? null : n.path("parentId").asLong()
        )
      );
    for (JsonNode n : api.get("/api/cities"))
      cities.add(
        new Option(n.path("id").asLong(), n.path("name").asText(), null)
      );
    categoryBox.getItems().setAll(new Option(0, "All categories", null));
    categoryBox.getItems().addAll(categories);
    categoryBox.getSelectionModel().selectFirst();
    cityBox.getItems().setAll(new Option(0, "All cities", null));
    cityBox.getItems().addAll(cities);
    cityBox.getSelectionModel().selectFirst();
    conditionBox
      .getItems()
      .setAll("Any condition", "New", "Like new", "Good", "Fair", "Needs repair");
    conditionBox.getSelectionModel().selectFirst();
    sortBox.getItems().setAll("Newest", "Price: low to high", "Price: high to low");
    sortBox.getSelectionModel().selectFirst();
  }

  @FXML
  private void search() {
    safe(() -> {
      StringBuilder path = new StringBuilder("/api/ads?sort=").append(
        sortValue(sortBox.getValue())
      );
      add(path, "keyword", keywordField.getText());
      add(path, "minPrice", minPriceField.getText());
      add(path, "maxPrice", maxPriceField.getText());
      if (
        categoryBox.getValue() != null && categoryBox.getValue().id() > 0
      ) add(path, "categoryId", String.valueOf(categoryBox.getValue().id()));
      if (cityBox.getValue() != null && cityBox.getValue().id() > 0) add(
        path,
        "cityId",
        String.valueOf(cityBox.getValue().id())
      );
      if (
        conditionBox.getValue() != null &&
        !conditionBox.getValue().startsWith("Any")
      ) add(path, "condition", conditionValue(conditionBox.getValue()));
      JsonNode result = api.get(path.toString());
      adsPane.getChildren().clear();
      for (JsonNode ad : result) adsPane.getChildren().add(card(ad));
      resultLabel.setText(result.size() + (result.size() == 1 ? " listing" : " listings"));
    });
  }

  private VBox card(JsonNode ad) {
    long id = ad.path("id").asLong();
    VBox box = new VBox(
      9,
      UiFactory.image(UiFactory.firstImage(ad), 320, 180),
      UiFactory.title(ad.path("title").asText()),
      styled(UiFactory.price(ad), "card-price"),
      new Label(
        ad.path("cityName").asText() +
          "  ·  " +
          ad.path("categoryName").asText()
      ),
      new Label(
        ad.path("sellerAverageRating").isNull()
          ? "New seller"
          : "★ " +
              String.format("%.1f", ad.path("sellerAverageRating").asDouble())
      ),
      UiFactory.action("View details", () -> NavigationManager.details(id))
    );
    box.getStyleClass().add("product-card");
    box.setPrefWidth(344);
    return box;
  }

  private Label styled(String text, String style) {
    Label label = new Label(text);
    label.getStyleClass().add(style);
    return label;
  }

  private String sortValue(String value) {
    if ("Price: low to high".equals(value)) return "cheapest";
    if ("Price: high to low".equals(value)) return "expensive";
    return "newest";
  }

  private String conditionValue(String value) {
    return Map.of(
      "New", "NEW",
      "Like new", "LIKE_NEW",
      "Good", "GOOD",
      "Fair", "FAIR",
      "Needs repair", "DAMAGED"
    ).get(value);
  }

  private void add(StringBuilder q, String key, String value) {
    if (value != null && !value.isBlank()) q.append("&")
      .append(key)
      .append("=")
      .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
  }

  @FXML
  private void create() {
    NavigationManager.adForm(null);
  }

  @FXML
  private void myAds() {
    NavigationManager.myAds();
  }

  @FXML
  private void favorites() {
    NavigationManager.favorites();
  }

  @FXML
  private void conversations() {
    NavigationManager.conversations();
  }

  @FXML
  private void admin() {
    NavigationManager.admin();
  }

  @FXML
  private void logout() {
    safe(() -> {
      api.post("/api/auth/logout", Map.of());
      SessionManager.clear();
      NavigationManager.mainAds();
    });
  }

  @FXML
  private void login() {
    NavigationManager.login();
  }
}
