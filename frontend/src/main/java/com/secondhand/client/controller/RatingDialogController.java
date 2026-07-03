package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.util.DialogUtils;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RatingDialogController extends BaseController {

  @FXML
  private ComboBox<Integer> ratingBox;

  @FXML
  private TextArea commentArea;

  private Stage stage;
  private JsonNode ad;

  @FXML
  private void initialize() {
    ratingBox.getItems().setAll(1, 2, 3, 4, 5);
    ratingBox.setValue(5);
  }

  public void initializeDialog(Stage stage, JsonNode ad) {
    this.stage = stage;
    this.ad = ad;
  }

  @FXML
  private void submit() {
    safe(() -> {
      api.post(
        "/api/ratings",
        Map.of(
          "advertisementId",
          ad.path("id").asLong(),
          "sellerId",
          ad.path("ownerId").asLong(),
          "rating",
          ratingBox.getValue(),
          "comment",
          commentArea.getText()
        )
      );
      DialogUtils.info("Thanks! Your rating was submitted.");
      stage.close();
    });
  }

  @FXML
  private void cancel() {
    stage.close();
  }
}
