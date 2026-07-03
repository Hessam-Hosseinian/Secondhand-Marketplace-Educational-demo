package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.model.Option;
import com.secondhand.client.util.DialogUtils;
import java.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

public class CreateEditAdController extends BaseController {

  @FXML
  private Label headingLabel;

  @FXML
  private TextField titleField, priceField, imageUrlField;

  @FXML
  private TextArea descriptionArea, attributesArea;

  @FXML
  private ComboBox<Option> mainCategoryBox, subcategoryBox, cityBox;

  @FXML
  private ComboBox<String> conditionBox;

  @FXML
  private Label errorLabel;

  private JsonNode existing;
  private final List<Option> categories = new ArrayList<>();

  @FXML
  private void initialize() {
    safe(() -> {
      for (JsonNode n : api.get("/api/categories"))
        categories.add(
          new Option(
            n.path("id").asLong(),
            n.path("name").asText(),
            n.path("parentId").isNull() ? null : n.path("parentId").asLong()
          )
        );
      mainCategoryBox.getItems().setAll(
        categories
          .stream()
          .filter(x -> x.parentId() == null)
          .toList()
      );
      for (JsonNode n : api.get("/api/cities"))
        cityBox
          .getItems()
          .add(
            new Option(n.path("id").asLong(), n.path("name").asText(), null)
          );
      conditionBox
        .getItems()
        .setAll("NEW", "LIKE_NEW", "GOOD", "FAIR", "DAMAGED");
      conditionBox.setValue("GOOD");
      if (!mainCategoryBox.getItems().isEmpty()) mainCategoryBox
        .getSelectionModel()
        .selectFirst();
      if (!cityBox.getItems().isEmpty()) cityBox
        .getSelectionModel()
        .selectFirst();
      mainChanged();
    });
  }

  public void load(JsonNode ad) {
    existing = ad;
    if (ad == null) return;
    headingLabel.setText("Edit advertisement");
    titleField.setText(ad.path("title").asText());
    priceField.setText(ad.path("price").asText());
    descriptionArea.setText(ad.path("description").asText());
    attributesArea.setText(ad.path("attributesText").asText());
    imageUrlField.setText(UiFactory.firstImage(ad));
    conditionBox.setValue(ad.path("itemCondition").asText());
    Option child = categories
      .stream()
      .filter(x -> x.id() == ad.path("categoryId").asLong())
      .findFirst()
      .orElse(null);
    if (child != null) {
      categories
        .stream()
        .filter(x -> Objects.equals(x.id(), child.parentId()))
        .findFirst()
        .ifPresent(mainCategoryBox::setValue);
      mainChanged();
      subcategoryBox.setValue(child);
    }
    cityBox
      .getItems()
      .stream()
      .filter(x -> x.id() == ad.path("cityId").asLong())
      .findFirst()
      .ifPresent(cityBox::setValue);
  }

  @FXML
  private void mainChanged() {
    subcategoryBox.getItems().clear();
    if (mainCategoryBox.getValue() != null) subcategoryBox.getItems().addAll(
      categories
        .stream()
        .filter(x ->
          Objects.equals(x.parentId(), mainCategoryBox.getValue().id())
        )
        .toList()
    );
    if (!subcategoryBox.getItems().isEmpty()) subcategoryBox
      .getSelectionModel()
      .selectFirst();
  }

  @FXML
  private void chooseImage() {
    safe(() -> {
      FileChooser chooser = new FileChooser();
      chooser.setTitle("Choose product image");
      chooser
        .getExtensionFilters()
        .add(
          new FileChooser.ExtensionFilter(
            "Images",
            "*.jpg",
            "*.jpeg",
            "*.png",
            "*.webp",
            "*.gif"
          )
        );
      var file = chooser.showOpenDialog(imageUrlField.getScene().getWindow());
      if (file != null) imageUrlField.setText(
        api.uploadImage(file).path("imageUrl").asText()
      );
    });
  }

  @FXML
  private void submit() {
    safe(() -> {
      errorLabel.setText("");
      if (
        titleField.getText().isBlank() ||
        descriptionArea.getText().isBlank() ||
        priceField.getText().isBlank() ||
        subcategoryBox.getValue() == null
      ) {
        errorLabel.setText("Please complete all required fields.");
        return;
      }
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("title", titleField.getText());
      body.put("description", descriptionArea.getText());
      body.put("price", priceField.getText());
      body.put("categoryId", subcategoryBox.getValue().id());
      body.put("cityId", cityBox.getValue().id());
      body.put("itemCondition", conditionBox.getValue());
      body.put("attributesText", attributesArea.getText());
      body.put("imageUrl", imageUrlField.getText());
      if (existing == null) api.post("/api/ads", body);
      else api.put("/api/ads/" + existing.path("id").asLong(), body);
      DialogUtils.info("Saved. Your advertisement is pending admin review.");
      NavigationManager.myAds();
    });
  }

  @FXML
  private void cancel() {
    NavigationManager.myAds();
  }
}
