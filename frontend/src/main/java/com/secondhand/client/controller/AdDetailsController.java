package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.auth.SessionManager;
import com.secondhand.client.util.DialogUtils;
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
  private Button favoriteButton, messageButton, ratingButton;

  @FXML
  private VBox ownerActions;

  private JsonNode ad;
  private boolean adminMode;

  public void load(long id, boolean admin) {
    adminMode = admin;
    safe(() -> {
      ad = api.get((admin ? "/api/admin/ads/" : "/api/ads/") + id);
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
      dateLabel.setText(ad.path("createdAt").asText().replace('T', ' '));
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
      imageView.setImage(
        UiFactory.image(UiFactory.firstImage(ad), 720, 390).getImage()
      );
      boolean own = ad.path("ownerId").asLong() == SessionManager.userId();
      favoriteButton.setVisible(!own && !admin);
      favoriteButton.setManaged(!own && !admin);
      messageButton.setVisible(!own && !admin);
      messageButton.setManaged(!own && !admin);
      ratingButton.setVisible(!own && !admin);
      ratingButton.setManaged(!own && !admin);
      ownerActions.setVisible(own && !admin);
      ownerActions.setManaged(own && !admin);
    });
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
      DialogUtils.info("Listing saved.");
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
    safe(() -> {
      api.put("/api/ads/" + ad.path("id").asLong() + "/sold", Map.of());
      DialogUtils.info("Listing marked as sold.");
      NavigationManager.myAds();
    });
  }

  @FXML
  private void delete() {
    safe(() -> {
      api.delete("/api/ads/" + ad.path("id").asLong());
      DialogUtils.info("Listing deleted.");
      NavigationManager.myAds();
    });
  }
}
