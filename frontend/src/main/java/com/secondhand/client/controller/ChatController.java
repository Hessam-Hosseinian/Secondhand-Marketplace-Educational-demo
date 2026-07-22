package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.auth.SessionManager;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ChatController extends BaseController {

  @FXML
  private VBox messagesBox;

  @FXML
  private TextField messageField;

  @FXML
  private ScrollPane scrollPane;

  private long conversationId;

  public void load(long id) {
    conversationId = id;
    refresh();
  }

  @FXML
  private void refresh() {
    if (conversationId == 0) return;
    safe(() -> {
      messagesBox.getChildren().clear();
      for (JsonNode m : api.get(
        "/api/conversations/" + conversationId + "/messages"
      )) {
        Label bubble = new Label(m.path("content").asText());
        bubble.setWrapText(true);
        bubble
          .getStyleClass()
          .add(
            m.path("senderId").asLong() == SessionManager.userId()
              ? "message-own"
              : "message-other"
          );
        VBox block = new VBox(
          4,
          new Label(m.path("senderName").asText()),
          bubble,
          new Label(m.path("createdAt").asText().replace('T', ' '))
        );
        block.setAlignment(
          m.path("senderId").asLong() == SessionManager.userId()
            ? Pos.CENTER_RIGHT
            : Pos.CENTER_LEFT
        );
        messagesBox.getChildren().add(block);
      }
      scrollPane.setVvalue(1);
    });
  }

  @FXML
  private void send() {
    safe(() -> {
      if (messageField.getText().isBlank()) return;
      api.post(
        "/api/conversations/" + conversationId + "/messages",
        Map.of("content", messageField.getText())
      );
      messageField.clear();
      refresh();
    });
  }

  @FXML
  private void back() {
    NavigationManager.conversations();
  }
}
