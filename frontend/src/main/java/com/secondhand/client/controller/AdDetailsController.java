package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.auth.SessionManager;
import com.secondhand.client.util.DialogUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class AdDetailsController extends BaseController {

  @FXML
  private ImageView imageView;

  @FXML
  private Label titleLabel, priceLabel, metaLabel, conditionLabel, sellerLabel, ratingLabel, dateLabel;

  @FXML
  private Label descriptionLabel, attributesLabel;

  @FXML
  private Button favoriteButton, messageButton, ratingButton,
    previousImageButton, nextImageButton;

  @FXML
  private Label imageCounterLabel;

  @FXML
  private VBox ownerActions, detailsLoading;

  @FXML
  private ScrollPane detailsContent;

  private JsonNode ad;
  private boolean adminMode;
  private final List<String> imageUrls = new ArrayList<>();
  private int imageIndex;

  public void load(long id, boolean admin) {
    adminMode = admin;
    setLoading(true);
    async(
      () -> api.get((admin ? "/api/admin/ads/" : "/api/ads/") + id),
      this::showAd,
      () -> setLoading(false)
    );
  }

  private void showAd(JsonNode value) {
    ad = value;
    titleLabel.setText(ad.path("title").asText());
    priceLabel.setText(UiFactory.price(ad));
    metaLabel.setText(
      ad.path("mainCategoryName").asText() +
        " / " +
        ad.path("categoryName").asText() +
        "  ·  " +
        ad.path("cityName").asText()
    );
    conditionLabel.setText(
      UiFactory.condition(ad.path("itemCondition").asText())
    );
    sellerLabel.setText(ad.path("ownerName").asText());
    double rating = ad.path("sellerAverageRating").asDouble(0);
    ratingLabel.setText(
      rating == 0
        ? "No ratings yet"
        : "★ " + String.format("%.1f", rating) + "  ·  " +
          ad.path("sellerRatingCount").asLong() + " reviews"
    );
    dateLabel.setText(formatDate(ad.path("createdAt").asText()));
    descriptionLabel.setText(ad.path("description").asText());
    StringBuilder attributes = new StringBuilder();
    ad.path("attributes").fields().forEachRemaining(entry ->
      attributes.append(entry.getKey()).append(": ")
        .append(entry.getValue().asText()).append("\n")
    );
    if (!ad.path("attributesText").asText("").isBlank()) {
      attributes.append(ad.path("attributesText").asText());
    }
    attributesLabel.setText(
      attributes.isEmpty() ? "No additional details provided." : attributes.toString()
    );
    imageUrls.clear();
    for (JsonNode image : ad.path("imageUrls")) {
      if (!image.asText().isBlank()) imageUrls.add(image.asText());
    }
    imageIndex = 0;
    showImage();
    boolean own = ad.path("ownerId").asLong() == SessionManager.userId();
    favoriteButton.setVisible(!own && !adminMode);
    favoriteButton.setManaged(!own && !adminMode);
    messageButton.setVisible(!own && !adminMode);
    messageButton.setManaged(!own && !adminMode);
    ratingButton.setVisible(!own && !adminMode);
    ratingButton.setManaged(!own && !adminMode);
    ownerActions.setVisible(own && !adminMode);
    ownerActions.setManaged(own && !adminMode);
  }

  private void setLoading(boolean loading) {
    detailsLoading.setVisible(loading);
    detailsLoading.setManaged(loading);
    detailsContent.setDisable(loading);
    detailsContent.setOpacity(loading ? 0.32 : 1);
  }

  private String formatDate(String value) {
    try {
      return LocalDateTime.parse(value).format(
        DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)
      );
    } catch (Exception ignored) {
      return value.replace('T', ' ');
    }
  }

  private void showImage() {
    boolean hasImages = !imageUrls.isEmpty();
    UiFactory.loadImage(
      imageView,
      hasImages ? imageUrls.get(imageIndex) : null,
      560,
      364
    );
    previousImageButton.setDisable(imageUrls.size() < 2);
    nextImageButton.setDisable(imageUrls.size() < 2);
    previousImageButton.setVisible(imageUrls.size() > 1);
    nextImageButton.setVisible(imageUrls.size() > 1);
    imageCounterLabel.setText(
      hasImages
        ? (imageIndex + 1) + " / " + imageUrls.size()
        : "No photo provided"
    );
  }

  @FXML
  private void previousImage() {
    if (imageUrls.size() < 2) return;
    imageIndex = (imageIndex - 1 + imageUrls.size()) % imageUrls.size();
    showImage();
  }

  @FXML
  private void nextImage() {
    if (imageUrls.size() < 2) return;
    imageIndex = (imageIndex + 1) % imageUrls.size();
    showImage();
  }

  @FXML
  private void back() {
    if (adminMode) NavigationManager.admin();
    else NavigationManager.mainAds();
  }

  @FXML
  private void favorite() {
    if (!NavigationManager.requireLogin()) return;
    safe(() -> {
      api.post("/api/favorites/" + ad.path("id").asLong(), Map.of());
      favoriteButton.setText("✓  Saved");
      favoriteButton.setDisable(true);
    });
  }

  @FXML
  private void message() {
    if (!NavigationManager.requireLogin()) return;
    safe(() -> {
      JsonNode c = api.post(
        "/api/conversations?adId=" + ad.path("id").asLong(),
        Map.of()
      );
      NavigationManager.chat(c.path("id").asLong());
    });
  }

  @FXML
  private void rate() {
    NavigationManager.rating(ad);
  }

  @FXML
  private void edit() {
    NavigationManager.adForm(ad);
  }

  @FXML
  private void sold() {
    if (!DialogUtils.confirm("Mark this listing as sold?")) return;
    safe(() -> {
      api.put("/api/ads/" + ad.path("id").asLong() + "/sold", Map.of());
      DialogUtils.info("Listing marked as sold.");
      NavigationManager.myAds();
    });
  }

  @FXML
  private void delete() {
    if (!DialogUtils.confirm("Delete this listing? This action cannot be undone.")) return;
    safe(() -> {
      api.delete("/api/ads/" + ad.path("id").asLong());
      DialogUtils.info("Listing deleted.");
      NavigationManager.myAds();
    });
  }
}
