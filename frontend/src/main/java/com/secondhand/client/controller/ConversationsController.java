package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.auth.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ConversationsController extends BaseController {

  @FXML
  private VBox listBox;

  @FXML
  private Label emptyLabel;

  @FXML
  private void initialize() {
    safe(() -> {
      JsonNode list = api.get("/api/conversations");
      emptyLabel.setVisible(list.isEmpty());
      emptyLabel.setManaged(list.isEmpty());
      for (JsonNode c : list) {
        long id = c.path("id").asLong();
        String other = c
          .path(
            c.path("buyerId").asLong() == SessionManager.userId()
              ? "sellerName"
              : "buyerName"
          )
          .asText();
        VBox details = new VBox(
          5,
          UiFactory.title(c.path("advertisementTitle").asText()),
          new Label("With " + other),
          new Label(c.path("lastMessage").asText("No messages yet"))
        );
        HBox row = UiFactory.row();
        row
          .getChildren()
          .addAll(
            details,
            UiFactory.spacer(),
            UiFactory.action("Open", () -> NavigationManager.chat(id))
          );
        listBox.getChildren().add(row);
      }
    });
  }

  @FXML
  private void back() {
    NavigationManager.mainAds();
  }
}
