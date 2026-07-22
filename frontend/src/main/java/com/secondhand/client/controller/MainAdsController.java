package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.api.ApiClient;
import com.secondhand.client.app.Animations;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.auth.SessionManager;
import com.secondhand.client.model.Option;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
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
    favoritesButton, messagesButton, searchButton;

  @FXML
  private Label userLabel, resultLabel;

  @FXML
  private VBox emptyState;

  @FXML
  private ProgressIndicator loadingIndicator;

  private final List<Option> categories = new ArrayList<>(),
    cities = new ArrayList<>();
  private int searchVersion;

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
      .setAll(
        "Any condition",
        "New",
        "Like new",
        "Good",
        "Fair",
        "Needs repair"
      );
    conditionBox.getSelectionModel().selectFirst();
    sortBox
      .getItems()
      .setAll("Newest", "Price: low to high", "Price: high to low");
    sortBox.getSelectionModel().selectFirst();
  }

  @FXML
  private void search() {
    safe(() -> {
      validatePriceRange();
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
      int requestVersion = ++searchVersion;
      setLoading(true);
      async(
        () -> api.get(path.toString()),
        result -> {
          if (requestVersion == searchVersion) showResults(result);
        },
        () -> {
          if (requestVersion == searchVersion) setLoading(false);
        }
      );
    });
  }

  private void showResults(JsonNode result) {
    adsPane.getChildren().clear();
    for (JsonNode ad : result) adsPane.getChildren().add(card(ad));
    boolean empty = result.isEmpty();
    adsPane.setVisible(!empty);
    adsPane.setManaged(!empty);
    emptyState.setVisible(empty);
    emptyState.setManaged(empty);
    resultLabel.setText(
      result.size() + (result.size() == 1 ? " match" : " matches")
    );
  }

  private void setLoading(boolean loading) {
    loadingIndicator.setVisible(loading);
    loadingIndicator.setManaged(loading);
    searchButton.setDisable(loading);
    adsPane.setOpacity(loading ? 0.5 : 1);
    resultLabel.setText(loading ? "Finding the best matches…" : resultLabel.getText());
  }

  @FXML
  private void clearFilters() {
    keywordField.clear();
    minPriceField.clear();
    maxPriceField.clear();
    categoryBox.getSelectionModel().selectFirst();
    cityBox.getSelectionModel().selectFirst();
    conditionBox.getSelectionModel().selectFirst();
    sortBox.getSelectionModel().selectFirst();
    search();
  }

  private void validatePriceRange() {
    BigDecimal min = price(minPriceField.getText(), "Minimum price");
    BigDecimal max = price(maxPriceField.getText(), "Maximum price");
    if (
      min != null && max != null && min.compareTo(max) > 0
    ) throw new ApiClient.ApiException(
      400,
      "Minimum price cannot exceed maximum price."
    );
  }

  private BigDecimal price(String value, String label) {
    if (value == null || value.isBlank()) return null;
    try {
      BigDecimal result = new BigDecimal(value.trim());
      if (result.signum() < 0) throw new NumberFormatException();
      return result;
    } catch (NumberFormatException e) {
      throw new ApiClient.ApiException(
        400,
        label + " must be a non-negative number."
      );
    }
  }

  private VBox card(JsonNode ad) {
    long id = ad.path("id").asLong();
    ImageView preview = UiFactory.image(UiFactory.firstImage(ad), 320, 190);
    Label title = UiFactory.title(ad.path("title").asText());
    preview.getStyleClass().add("card-link");
    title.getStyleClass().add("card-link");
    preview.setOnMouseClicked(event -> NavigationManager.details(id));
    title.setOnMouseClicked(event -> NavigationManager.details(id));

    Label metaLabel = styled(
      ad.path("cityName").asText() + "  ·  " + ad.path("categoryName").asText(),
      "card-meta"
    );

    Label conditionLabel = styled(
      UiFactory.condition(ad.path("itemCondition").asText()),
      "condition-badge"
    );

    Label ratingLabel = new Label(
      ad.path("sellerAverageRating").isNull()
        ? "New seller"
        : "★ " +
            String.format("%.1f", ad.path("sellerAverageRating").asDouble())
    );
    ratingLabel
      .getStyleClass()
      .add(
        ad.path("sellerAverageRating").isNull() ? "card-meta" : "rating-chip"
      );

    HBox priceRow = new HBox(
      8,
      styled(UiFactory.price(ad), "card-price"),
      UiFactory.spacer(),
      ratingLabel
    );
    priceRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

    HBox metaRow = new HBox(8, metaLabel, UiFactory.spacer(), conditionLabel);
    metaRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

    Button viewButton = new Button("View details");
    viewButton.setOnAction(e -> {
      NavigationManager.details(id);
    });
    viewButton.getStyleClass().add("secondary-button");
    viewButton.setMaxWidth(Double.MAX_VALUE);
    viewButton.setTooltip(new Tooltip("Open listing details"));

    VBox box = new VBox(
      10,
      preview,
      title,
      metaRow,
      priceRow,
      viewButton
    );
    box.getStyleClass().add("product-card");
    box.setPrefWidth(344);
    Animations.hoverLift(box);
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
      "New",
      "NEW",
      "Like new",
      "LIKE_NEW",
      "Good",
      "GOOD",
      "Fair",
      "FAIR",
      "Needs repair",
      "DAMAGED"
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
    SessionManager.clear();
    NavigationManager.mainAds();
  }

  @FXML
  private void login() {
    NavigationManager.login();
  }
}
