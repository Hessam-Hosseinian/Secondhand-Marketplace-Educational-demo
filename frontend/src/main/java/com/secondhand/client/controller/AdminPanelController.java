package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.auth.SessionManager;
import com.secondhand.client.model.Option;
import com.secondhand.client.util.DialogUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

public class AdminPanelController extends BaseController {

  @FXML
  private VBox pendingBox, usersBox, categoriesBox, citiesBox, adminLoading;

  @FXML
  private Label pendingCountLabel, usersCountLabel, categoriesCountLabel,
    citiesCountLabel, adminStatusLabel;

  @FXML
  private TextField categoryNameField, cityNameField, provinceField;

  @FXML
  private ComboBox<Option> parentCategoryBox;

  @FXML
  private Button refreshButton;

  @FXML
  private TabPane adminTabPane;

  private record AdminData(
    JsonNode pending,
    JsonNode users,
    JsonNode categories,
    JsonNode cities
  ) {}

  @FXML
  private void initialize() {
    refresh();
  }

  @FXML
  private void refresh() {
    setLoading(true);
    adminStatusLabel.setText("Refreshing marketplace data…");
    async(
      this::fetchData,
      data -> {
        render(data);
        adminStatusLabel.setText("All marketplace data is up to date");
      },
      () -> setLoading(false)
    );
  }

  private AdminData fetchData() {
    return new AdminData(
      api.get("/api/admin/ads/pending"),
      api.get("/api/admin/users"),
      api.get("/api/categories"),
      api.get("/api/cities")
    );
  }

  private void render(AdminData data) {
    pendingCountLabel.setText(String.valueOf(data.pending().size()));
    usersCountLabel.setText(String.valueOf(data.users().size()));
    categoriesCountLabel.setText(String.valueOf(data.categories().size()));
    citiesCountLabel.setText(String.valueOf(data.cities().size()));
    loadPending(data.pending());
    loadUsers(data.users());
    loadCategories(data.categories());
    loadCities(data.cities());
  }

  private void setLoading(boolean loading) {
    adminLoading.setVisible(loading);
    adminLoading.setManaged(loading);
    adminTabPane.setDisable(loading);
    adminTabPane.setOpacity(loading ? 0.38 : 1);
    refreshButton.setDisable(loading);
  }

  private void loadPending(JsonNode pending) {
    pendingBox.getChildren().clear();
    if (pending.isEmpty()) {
      pendingBox.getChildren().add(
        emptyState("✓", "Review queue is clear", "New submissions will appear here for moderation.")
      );
      return;
    }

    for (JsonNode ad : pending) {
      long id = ad.path("id").asLong();
      ImageView image = UiFactory.image(UiFactory.firstImage(ad), 104, 78);
      StackPane thumbnail = new StackPane(image);
      thumbnail.getStyleClass().add("admin-thumbnail");

      Label title = UiFactory.title(ad.path("title").asText());
      title.getStyleClass().add("admin-item-title");
      Label seller = label(
        "By " + ad.path("ownerName").asText() + "  ·  " + ad.path("cityName").asText(),
        "admin-row-meta"
      );
      HBox details = new HBox(
        8,
        badge(UiFactory.condition(ad.path("itemCondition").asText()), "info-chip"),
        badge(UiFactory.price(ad), "price-chip")
      );
      VBox copy = new VBox(5, title, seller, details);
      HBox.setHgrow(copy, Priority.ALWAYS);

      HBox actions = new HBox(
        7,
        action("View", "neutral-admin-action", () -> NavigationManager.adminDetails(id)),
        action("Approve", "approve-admin-action", () ->
          runAdminAction(
            () -> api.put("/api/admin/ads/" + id + "/approve", Map.of()),
            null,
            "Listing approved"
          )
        ),
        action("Reject", "reject-admin-action", () -> reject(id)),
        action("Delete", "delete-admin-action", () -> deleteListing(id))
      );
      actions.setAlignment(Pos.CENTER_RIGHT);
      actions.getStyleClass().add("admin-row-actions");

      HBox row = UiFactory.row();
      row.getStyleClass().add("admin-review-row");
      row.getChildren().addAll(thumbnail, copy, actions);
      pendingBox.getChildren().add(row);
    }
  }

  private void reject(long id) {
    DialogUtils.prompt(
      "Reject listing",
      "What should the seller correct?",
      "Give a concise and helpful reason…"
    ).ifPresent(reason ->
      runAdminAction(
        () -> api.put("/api/admin/ads/" + id + "/reject", Map.of("reason", reason)),
        null,
        "Listing returned to the seller"
      )
    );
  }

  private void deleteListing(long id) {
    if (!DialogUtils.confirm("Delete this listing from the marketplace?")) return;
    runAdminAction(
      () -> api.delete("/api/admin/ads/" + id),
      null,
      "Listing deleted"
    );
  }

  private void loadUsers(JsonNode users) {
    usersBox.getChildren().clear();
    if (users.isEmpty()) {
      usersBox.getChildren().add(
        emptyState("◎", "No user accounts", "Registered users will appear here.")
      );
      return;
    }

    for (JsonNode user : users) {
      long id = user.path("id").asLong();
      boolean active = "ACTIVE".equals(user.path("status").asText());
      boolean currentUser = id == SessionManager.userId();

      Label avatar = label(initials(user.path("fullName").asText()), "admin-avatar");
      Label name = UiFactory.title(user.path("fullName").asText());
      name.getStyleClass().add("admin-item-title");
      Label contact = label(userContact(user), "admin-row-meta");
      HBox chips = new HBox(
        7,
        badge(user.path("role").asText(), "role-chip"),
        badge(active ? "Active" : "Blocked", active ? "active-chip" : "blocked-chip"),
        badge(user.path("accountType").asText(), "info-chip")
      );
      VBox copy = new VBox(5, name, contact, chips);
      HBox.setHgrow(copy, Priority.ALWAYS);

      Button accountAction = action(
        active ? "Block account" : "Unblock account",
        active ? "reject-admin-action" : "approve-admin-action",
        () -> toggleUser(id, active, user.path("fullName").asText())
      );
      accountAction.setDisable(currentUser);
      if (currentUser) accountAction.setText("Current admin");

      HBox row = UiFactory.row();
      row.getStyleClass().add("admin-user-row");
      row.getChildren().addAll(avatar, copy, accountAction);
      usersBox.getChildren().add(row);
    }
  }

  private void toggleUser(long id, boolean active, String name) {
    String verb = active ? "block" : "unblock";
    if (!DialogUtils.confirm("Do you want to " + verb + " " + name + "?")) return;
    runAdminAction(
      () -> api.put(
        "/api/admin/users/" + id + (active ? "/block" : "/unblock"),
        Map.of()
      ),
      null,
      "User account updated"
    );
  }

  private void loadCategories(JsonNode categories) {
    categoriesBox.getChildren().clear();
    parentCategoryBox.getItems().clear();
    parentCategoryBox.getItems().add(new Option(0, "Top-level category", null));
    List<Option> options = new ArrayList<>();
    for (JsonNode category : categories) {
      options.add(new Option(
        category.path("id").asLong(),
        category.path("name").asText(),
        category.path("parentId").isNull() ? null : category.path("parentId").asLong()
      ));
    }
    options.sort(
      Comparator
        .comparingLong((Option option) ->
          option.parentId() == null ? option.id() : option.parentId()
        )
        .thenComparingInt(option -> option.parentId() == null ? 0 : 1)
        .thenComparing(Option::name, String.CASE_INSENSITIVE_ORDER)
    );
    for (Option option : options) {
      if (option.parentId() == null) parentCategoryBox.getItems().add(option);

      Label icon = label(option.parentId() == null ? "◆" : "↳", "taxonomy-icon");
      VBox copy = new VBox(
        3,
        label(option.name(), "taxonomy-name"),
        label(option.parentId() == null ? "Main category" : "Subcategory", "admin-row-meta")
      );
      HBox.setHgrow(copy, Priority.ALWAYS);
      Button disable = action("Disable", "delete-admin-action", () -> {
        if (!DialogUtils.confirm("Disable the “" + option.name() + "” category?")) return;
        runAdminAction(
          () -> api.delete("/api/admin/categories/" + option.id()),
          null,
          "Category disabled"
        );
      });
      HBox row = UiFactory.row();
      if (option.parentId() != null) row.getStyleClass().add("taxonomy-child");
      row.getChildren().addAll(icon, copy, disable);
      categoriesBox.getChildren().add(row);
    }
    if (categories.isEmpty()) {
      categoriesBox.getChildren().add(
        emptyState("◇", "No active categories", "Create the first marketplace category above.")
      );
    }
    parentCategoryBox.getSelectionModel().selectFirst();
  }

  private void loadCities(JsonNode cities) {
    citiesBox.getChildren().clear();
    for (JsonNode city : cities) {
      long id = city.path("id").asLong();
      String name = city.path("name").asText();
      VBox copy = new VBox(
        3,
        label(name, "taxonomy-name"),
        label(city.path("province").asText(), "admin-row-meta")
      );
      HBox.setHgrow(copy, Priority.ALWAYS);
      Button disable = action("Disable", "delete-admin-action", () -> {
        if (!DialogUtils.confirm("Disable service in “" + name + "”?")) return;
        runAdminAction(
          () -> api.delete("/api/admin/cities/" + id),
          null,
          "City disabled"
        );
      });
      HBox row = UiFactory.row();
      row.getChildren().addAll(label("⌖", "taxonomy-icon"), copy, disable);
      citiesBox.getChildren().add(row);
    }
    if (cities.isEmpty()) {
      citiesBox.getChildren().add(
        emptyState("⌖", "No service locations", "Add a city and province above.")
      );
    }
  }

  @FXML
  private void addCategory() {
    String name = categoryNameField.getText().trim();
    if (name.isBlank()) {
      DialogUtils.error(new IllegalArgumentException("Enter a category name."));
      categoryNameField.requestFocus();
      return;
    }
    Map<String, Object> body = new HashMap<>();
    body.put("name", name);
    if (parentCategoryBox.getValue() != null && parentCategoryBox.getValue().id() > 0) {
      body.put("parentId", parentCategoryBox.getValue().id());
    }
    runAdminAction(
      () -> api.post("/api/admin/categories", body),
      categoryNameField::clear,
      "Category added"
    );
  }

  @FXML
  private void addCity() {
    String city = cityNameField.getText().trim();
    String province = provinceField.getText().trim();
    if (city.isBlank() || province.isBlank()) {
      DialogUtils.error(new IllegalArgumentException("Enter both city and province."));
      (city.isBlank() ? cityNameField : provinceField).requestFocus();
      return;
    }
    runAdminAction(
      () -> api.post(
        "/api/admin/cities",
        Map.of("name", city, "province", province)
      ),
      () -> {
        cityNameField.clear();
        provinceField.clear();
      },
      "Service location added"
    );
  }

  private void runAdminAction(Runnable operation, Runnable afterSuccess, String message) {
    setLoading(true);
    adminStatusLabel.setText("Applying changes…");
    async(
      () -> {
        operation.run();
        return fetchData();
      },
      data -> {
        render(data);
        if (afterSuccess != null) afterSuccess.run();
        adminStatusLabel.setText("✓  " + message);
      },
      () -> setLoading(false)
    );
  }

  private Button action(String text, String styleClass, Runnable handler) {
    Button button = UiFactory.action(text, handler);
    button.getStyleClass().addAll("admin-action", styleClass);
    return button;
  }

  private Label badge(String text, String styleClass) {
    return label(text == null || text.isBlank() ? "—" : text, styleClass);
  }

  private Label label(String text, String styleClass) {
    Label label = new Label(text == null ? "" : text);
    label.getStyleClass().add(styleClass);
    return label;
  }

  private VBox emptyState(String icon, String title, String description) {
    VBox empty = new VBox(
      7,
      label(icon, "admin-empty-icon"),
      label(title, "admin-empty-title"),
      label(description, "admin-row-meta")
    );
    empty.setAlignment(Pos.CENTER);
    empty.getStyleClass().add("admin-empty-state");
    return empty;
  }

  private String initials(String fullName) {
    String value = fullName == null ? "" : fullName.trim();
    if (value.isBlank()) return "?";
    String[] parts = value.split("\\s+");
    return parts.length == 1
      ? parts[0].substring(0, 1).toUpperCase()
      : (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
  }

  private String userContact(JsonNode user) {
    String username = "@" + user.path("username").asText();
    String email = user.path("email").asText("");
    String phone = user.path("phoneNumber").asText("");
    if (!email.isBlank()) return username + "  ·  " + email;
    if (!phone.isBlank()) return username + "  ·  " + phone;
    return username;
  }

  @FXML
  private void back() {
    NavigationManager.mainAds();
  }
}
