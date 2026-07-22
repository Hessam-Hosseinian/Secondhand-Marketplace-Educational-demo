package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.util.DialogUtils;
import java.util.LinkedHashMap;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class MyAdsController extends BaseController {

  @FXML
  private VBox listBox, listingsLoading, listingsEmpty;

  @FXML
  private Label countLabel, allCountLabel, activeCountLabel, pendingCountLabel,
    attentionCountLabel;

  @FXML
  private ComboBox<String> statusFilter;

  @FXML
  private Button refreshButton;

  @FXML
  private ScrollPane listingsContent;

  private JsonNode allAds;

  private static final Map<String, String> FILTERS = new LinkedHashMap<>();

  static {
    FILTERS.put("All listings", null);
    FILTERS.put("Active", "ACTIVE");
    FILTERS.put("In review", "PENDING");
    FILTERS.put("Needs changes", "REJECTED");
    FILTERS.put("Sold", "SOLD");
    FILTERS.put("Deleted", "DELETED");
  }

  @FXML
  private void initialize() {
    statusFilter.getItems().setAll(FILTERS.keySet());
    statusFilter.getSelectionModel().selectFirst();
    refresh();
  }

  @FXML
  private void refresh() {
    setLoading(true);
    async(
      () -> api.get("/api/my/ads"),
      this::showAds,
      () -> setLoading(false)
    );
  }

  private void showAds(JsonNode ads) {
    allAds = ads;
    allCountLabel.setText(String.valueOf(ads.size()));
    activeCountLabel.setText(String.valueOf(count("ACTIVE")));
    pendingCountLabel.setText(String.valueOf(count("PENDING")));
    attentionCountLabel.setText(String.valueOf(count("REJECTED")));
    applyFilter();
  }

  @FXML
  private void applyFilter() {
    if (allAds == null) return;
    String selectedStatus = FILTERS.get(statusFilter.getValue());
    listBox.getChildren().clear();
    int visible = 0;
    for (JsonNode ad : allAds) {
      if (
        selectedStatus == null || selectedStatus.equals(ad.path("status").asText())
      ) {
        listBox.getChildren().add(row(ad));
        visible++;
      }
    }
    countLabel.setText(visible + (visible == 1 ? " listing" : " listings"));
    boolean empty = visible == 0;
    listingsEmpty.setVisible(empty);
    listingsEmpty.setManaged(empty);
    listBox.setVisible(!empty);
    listBox.setManaged(!empty);
  }

  private long count(String status) {
    long result = 0;
    for (JsonNode ad : allAds) {
      if (status.equals(ad.path("status").asText())) result++;
    }
    return result;
  }

  private HBox row(JsonNode ad) {
    long id = ad.path("id").asLong();
    String status = ad.path("status").asText();

    Label statusLabel = new Label(UiFactory.status(status));
    statusLabel.getStyleClass().addAll(
      "my-listing-status",
      "listing-status-" + status.toLowerCase()
    );
    Label title = UiFactory.title(ad.path("title").asText());
    Label meta = new Label(
      ad.path("categoryName").asText() + "  ·  " + ad.path("cityName").asText()
    );
    meta.getStyleClass().add("admin-row-meta");
    HBox badges = new HBox(
      7,
      statusLabel,
      chip(UiFactory.condition(ad.path("itemCondition").asText()), "info-chip")
    );
    VBox text = new VBox(6, title, meta, badges);
    if ("REJECTED".equals(status)) {
      Label reason = new Label(
        "Needs attention: " + ad.path("rejectionReason").asText("Review the listing details.")
      );
      reason.setWrapText(true);
      reason.getStyleClass().add("listing-rejection-note");
      text.getChildren().add(reason);
    }
    HBox.setHgrow(text, Priority.ALWAYS);

    Label price = new Label(UiFactory.price(ad));
    price.getStyleClass().add("my-listing-price");
    Button edit = action("Edit", "neutral-admin-action", () -> NavigationManager.adForm(ad));
    Button sold = action("Mark sold", "approve-admin-action", () -> markSold(id));
    Button delete = action("Delete", "delete-admin-action", () -> delete(id));
    sold.setDisable(!"ACTIVE".equals(status));
    edit.setDisable("SOLD".equals(status) || "DELETED".equals(status));
    delete.setDisable("DELETED".equals(status));
    HBox actions = new HBox(7, edit, sold, delete);
    actions.setAlignment(Pos.CENTER_RIGHT);
    VBox controls = new VBox(10, price, actions);
    controls.setAlignment(Pos.CENTER_RIGHT);

    HBox row = UiFactory.row();
    row.getStyleClass().add("my-listing-row");
    row.getChildren().addAll(
      UiFactory.image(UiFactory.firstImage(ad), 142, 96),
      text,
      controls
    );
    return row;
  }

  private Label chip(String text, String styleClass) {
    Label label = new Label(text);
    label.getStyleClass().add(styleClass);
    return label;
  }

  private Button action(String text, String styleClass, Runnable handler) {
    Button button = UiFactory.action(text, handler);
    button.getStyleClass().addAll("admin-action", styleClass);
    return button;
  }

  private void markSold(long id) {
    if (!DialogUtils.confirm("Mark this listing as sold?")) return;
    runAction(
      () -> api.put("/api/ads/" + id + "/sold", Map.of()),
      "Listing marked as sold."
    );
  }

  private void delete(long id) {
    if (!DialogUtils.confirm("Delete this listing? This action cannot be undone.")) return;
    runAction(() -> api.delete("/api/ads/" + id), "Listing deleted.");
  }

  private void runAction(Runnable operation, String message) {
    setLoading(true);
    async(
      () -> {
        operation.run();
        return api.get("/api/my/ads");
      },
      ads -> {
        showAds(ads);
        DialogUtils.info(message);
      },
      () -> setLoading(false)
    );
  }

  private void setLoading(boolean loading) {
    listingsLoading.setVisible(loading);
    listingsLoading.setManaged(loading);
    listingsContent.setDisable(loading);
    listingsContent.setOpacity(loading ? 0.35 : 1);
    refreshButton.setDisable(loading);
  }

  @FXML
  private void create() {
    NavigationManager.adForm(null);
  }

  @FXML
  private void back() {
    NavigationManager.mainAds();
  }
}
