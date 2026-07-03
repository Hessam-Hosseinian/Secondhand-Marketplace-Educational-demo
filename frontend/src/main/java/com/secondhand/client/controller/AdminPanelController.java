package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.auth.SessionManager;
import com.secondhand.client.model.Option;
import java.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class AdminPanelController extends BaseController {

  @FXML
  private VBox pendingBox, usersBox, categoriesBox, citiesBox;

  @FXML
  private TextField categoryNameField, cityNameField, provinceField;

  @FXML
  private ComboBox<Option> parentCategoryBox;

  @FXML
  private void initialize() {
    refresh();
  }

  private void refresh() {
    safe(() -> {
      loadPending();
      loadUsers();
      loadCategories();
      loadCities();
    });
  }

  private void loadPending() {
    pendingBox.getChildren().clear();
    for (JsonNode ad : api.get("/api/admin/ads/pending")) {
      long id = ad.path("id").asLong();
      HBox row = UiFactory.row();
      row.getChildren().addAll(
        new VBox(
          4,
          UiFactory.title(ad.path("title").asText()),
          new Label(
            ad.path("ownerName").asText() + "  ·  " + UiFactory.price(ad)
          )
        ),
        UiFactory.spacer(),
        UiFactory.action("View", () -> NavigationManager.adminDetails(id)),
        UiFactory.action("Approve", () ->
          safe(() -> {
            api.put("/api/admin/ads/" + id + "/approve", Map.of());
            refresh();
          })
        ),
        UiFactory.action("Reject", () -> reject(id)),
        UiFactory.action("Delete", () ->
          safe(() -> {
            api.delete("/api/admin/ads/" + id);
            refresh();
          })
        )
      );
      pendingBox.getChildren().add(row);
    }
  }

  private void reject(long id) {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Reject advertisement");
    dialog.setHeaderText("Tell the seller what should be corrected");
    dialog
      .showAndWait()
      .filter(x -> !x.isBlank())
      .ifPresent(reason ->
        safe(() -> {
          api.put("/api/admin/ads/" + id + "/reject", Map.of("reason", reason));
          refresh();
        })
      );
  }

  private void loadUsers() {
    usersBox.getChildren().clear();
    for (JsonNode user : api.get("/api/admin/users")) {
      long id = user.path("id").asLong();
      String status = user.path("status").asText();
      Button action = UiFactory.action(
        "ACTIVE".equals(status) ? "Block" : "Unblock",
        () ->
          safe(() -> {
            api.put(
              "/api/admin/users/" +
                id +
                ("ACTIVE".equals(status) ? "/block" : "/unblock"),
              Map.of()
            );
            refresh();
          })
      );
      action.setDisable(id == SessionManager.userId());
      HBox row = UiFactory.row();
      row
        .getChildren()
        .addAll(
          new VBox(
            4,
            UiFactory.title(user.path("fullName").asText()),
            new Label(
              "@" +
                user.path("username").asText() +
                "  ·  " +
                user.path("role").asText() +
                "  ·  " +
                status
            )
          ),
          UiFactory.spacer(),
          action
        );
      usersBox.getChildren().add(row);
    }
  }

  private void loadCategories() {
    categoriesBox.getChildren().clear();
    parentCategoryBox.getItems().clear();
    parentCategoryBox.getItems().add(new Option(0, "Main category", null));
    for (JsonNode category : api.get("/api/categories")) {
      Option option = new Option(
        category.path("id").asLong(),
        category.path("name").asText(),
        category.path("parentId").isNull()
          ? null
          : category.path("parentId").asLong()
      );
      if (option.parentId() == null) parentCategoryBox.getItems().add(option);
      HBox row = UiFactory.row();
      row.getChildren().addAll(
        new Label(
          (option.parentId() == null ? "◆  " : "    ↳  ") + option.name()
        ),
        UiFactory.spacer(),
        UiFactory.action("Disable", () ->
          safe(() -> {
            api.delete("/api/admin/categories/" + option.id());
            refresh();
          })
        )
      );
      categoriesBox.getChildren().add(row);
    }
    parentCategoryBox.getSelectionModel().selectFirst();
  }

  private void loadCities() {
    citiesBox.getChildren().clear();
    for (JsonNode city : api.get("/api/cities")) {
      long id = city.path("id").asLong();
      HBox row = UiFactory.row();
      row.getChildren().addAll(
        new Label(
          city.path("name").asText() + "  ·  " + city.path("province").asText()
        ),
        UiFactory.spacer(),
        UiFactory.action("Disable", () ->
          safe(() -> {
            api.delete("/api/admin/cities/" + id);
            refresh();
          })
        )
      );
      citiesBox.getChildren().add(row);
    }
  }

  @FXML
  private void addCategory() {
    safe(() -> {
      Map<String, Object> body = new HashMap<>();
      body.put("name", categoryNameField.getText());
      if (
        parentCategoryBox.getValue() != null &&
        parentCategoryBox.getValue().id() > 0
      ) body.put("parentId", parentCategoryBox.getValue().id());
      api.post("/api/admin/categories", body);
      categoryNameField.clear();
      refresh();
    });
  }

  @FXML
  private void addCity() {
    safe(() -> {
      api.post(
        "/api/admin/cities",
        Map.of(
          "name",
          cityNameField.getText(),
          "province",
          provinceField.getText()
        )
      );
      cityNameField.clear();
      provinceField.clear();
      refresh();
    });
  }

  @FXML
  private void back() {
    NavigationManager.mainAds();
  }
}
