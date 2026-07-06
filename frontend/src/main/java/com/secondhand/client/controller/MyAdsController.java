package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.app.Animations;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.util.DialogUtils;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class MyAdsController extends BaseController {

  @FXML
  private VBox listBox;

  @FXML
  private Label countLabel;

  @FXML
  private void initialize() {
    refresh();
  }

  private void refresh() {
    safe(() -> {
      JsonNode ads = api.get("/api/my/ads");
      countLabel.setText(ads.size() + (ads.size() == 1 ? " listing" : " listings"));
      listBox.getChildren().clear();
      for (JsonNode ad : ads) listBox.getChildren().add(row(ad));
      Animations.stagger(listBox.getChildren());
    });
  }

  private HBox row(JsonNode ad) {
    long id = ad.path("id").asLong();
    String status = ad.path("status").asText();
    VBox text = new VBox(
      5,
      UiFactory.title(ad.path("title").asText()),
      new Label(UiFactory.price(ad) + "  ·  " + UiFactory.status(status))
    );
    if ("REJECTED".equals(status)) text
      .getChildren()
      .add(new Label("Rejection reason: " + ad.path("rejectionReason").asText()));
    Button edit = UiFactory.action("Edit", () -> NavigationManager.adForm(ad));
    Button sold = UiFactory.action("Mark sold", () ->
      safe(() -> {
        api.put("/api/ads/" + id + "/sold", Map.of());
        refresh();
      })
    );
    sold.setDisable(!"ACTIVE".equals(status));
    edit.setDisable("SOLD".equals(status) || "DELETED".equals(status));
    HBox row = UiFactory.row();
    row.getChildren().addAll(
      UiFactory.image(UiFactory.firstImage(ad), 126, 82),
      text,
      UiFactory.spacer(),
      edit,
      sold,
      UiFactory.action("Delete", () ->
        safe(() -> {
          api.delete("/api/ads/" + id);
          DialogUtils.info("Listing deleted.");
          refresh();
        })
      )
    );
    Animations.hoverLift(row);
    return row;
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
