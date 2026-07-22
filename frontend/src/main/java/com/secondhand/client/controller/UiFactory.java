package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import java.text.DecimalFormat;
import java.util.Map;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;

final class UiFactory {

  private UiFactory() {}

  static ImageView image(String url, double w, double h) {
    ImageView view = new ImageView();
    loadImage(view, url, w, h);
    return view;
  }

  static void loadImage(ImageView view, String url, double w, double h) {
    view.setFitWidth(w);
    view.setFitHeight(h);
    view.setPreserveRatio(false);
    view.setSmooth(true);
    if (!view.getStyleClass().contains("ad-image")) {
      view.getStyleClass().add("ad-image");
    }
    Rectangle clip = new Rectangle(w, h);
    clip.setArcWidth(24);
    clip.setArcHeight(24);
    view.setClip(clip);
    if (url != null && !url.isBlank()) {
      Image image = new Image(url, true);
      view.setImage(image);
      Runnable crop = () -> {
        if (view.getImage() == image) cropToFill(view, image, w, h);
      };
      image.widthProperty().addListener((observable, oldValue, newValue) ->
        crop.run()
      );
      image.heightProperty().addListener((observable, oldValue, newValue) ->
        crop.run()
      );
      crop.run();
      image.errorProperty().addListener((o, a, b) -> {
        if (b && view.getImage() == image) {
          view.setImage(null);
          view.setViewport(null);
        }
      });
    } else {
      view.setImage(null);
      view.setViewport(null);
    }
  }

  private static void cropToFill(
    ImageView view,
    Image image,
    double targetWidth,
    double targetHeight
  ) {
    double width = image.getWidth();
    double height = image.getHeight();
    if (width <= 0 || height <= 0) return;
    double targetRatio = targetWidth / targetHeight;
    double imageRatio = width / height;
    if (imageRatio > targetRatio) {
      double cropWidth = height * targetRatio;
      view.setViewport(
        new Rectangle2D((width - cropWidth) / 2, 0, cropWidth, height)
      );
    } else {
      double cropHeight = width / targetRatio;
      view.setViewport(
        new Rectangle2D(0, (height - cropHeight) / 2, width, cropHeight)
      );
    }
  }

  static String firstImage(JsonNode ad) {
    return ad.path("imageUrls").size() > 0
      ? ad.path("imageUrls").get(0).asText()
      : null;
  }

  static String price(JsonNode ad) {
    return (
      new DecimalFormat("#,###").format(ad.path("price").asDouble()) + " IRR"
    );
  }

  static Region spacer() {
    Region r = new Region();
    HBox.setHgrow(r, Priority.ALWAYS);
    return r;
  }

  static HBox row() {
    HBox row = new HBox(14);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("list-row");
    return row;
  }

  static Button action(String text, Runnable action) {
    Button b = new Button(text);
    b.setOnAction(e -> action.run());
    return b;
  }

  static Label title(String value) {
    Label label = new Label(value);
    label.getStyleClass().add("card-title");
    label.setWrapText(true);
    label.setMaxWidth(Double.MAX_VALUE);
    return label;
  }

  static String condition(String value) {
    return Map.of(
      "NEW", "New",
      "LIKE_NEW", "Like new",
      "GOOD", "Good",
      "FAIR", "Fair",
      "DAMAGED", "Needs repair"
    ).getOrDefault(value, value);
  }

  static String status(String value) {
    return Map.of(
      "PENDING", "Pending review",
      "ACTIVE", "Active",
      "REJECTED", "Rejected",
      "DELETED", "Deleted",
      "SOLD", "Sold"
    ).getOrDefault(value, value);
  }
}
