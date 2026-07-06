package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.app.Animations;
import com.secondhand.client.app.NavigationManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class FavoritesController extends BaseController {

  @FXML
  private VBox listBox;

  @FXML
  private Label emptyLabel;

  @FXML
  private void initialize() {
    refresh();
  }

  private void refresh() {
    safe(() -> {
      JsonNode list = api.get("/api/favorites");
      listBox.getChildren().clear();
      emptyLabel.setVisible(list.isEmpty());
      emptyLabel.setManaged(list.isEmpty());
      for (JsonNode ad : list) {
        long id = ad.path("id").asLong();
        HBox row = UiFactory.row();
        row.getChildren().addAll(
          UiFactory.image(UiFactory.firstImage(ad), 126, 82),
          new VBox(
            5,
            UiFactory.title(ad.path("title").asText()),
            new Label(
              UiFactory.price(ad) + "  ·  " + ad.path("cityName").asText()
            )
          ),
          UiFactory.spacer(),
          UiFactory.action("View", () -> NavigationManager.details(id)),
          UiFactory.action("Remove", () ->
            safe(() -> {
              api.delete("/api/favorites/" + id);
              refresh();
            })
          )
        );
        Animations.hoverLift(row);
        listBox.getChildren().add(row);
      }
      Animations.stagger(listBox.getChildren());
    });
  }

  @FXML
  private void back() {
    NavigationManager.mainAds();
  }
}
