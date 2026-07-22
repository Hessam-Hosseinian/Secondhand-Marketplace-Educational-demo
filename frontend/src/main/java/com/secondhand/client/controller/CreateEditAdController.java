package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.api.ApiClient;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.model.Option;
import com.secondhand.client.util.DialogUtils;
import java.util.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

public class CreateEditAdController extends BaseController {

  @FXML
  private Label headingLabel;

  @FXML
  private TextField titleField, priceField;

  @FXML
  private TextArea descriptionArea, attributesArea;

  @FXML
  private ComboBox<Option> mainCategoryBox, subcategoryBox, cityBox;

  @FXML
  private ComboBox<String> conditionBox;

  @FXML
  private Label errorLabel;

  @FXML
  private VBox dynamicAttributesBox;

  @FXML
  private ListView<String> imageList;

  private JsonNode existing;
  private final List<Option> categories = new ArrayList<>();
  private final Map<String, Control> attributeInputs = new LinkedHashMap<>();
  private final Map<String, String> attributeLabels = new LinkedHashMap<>();
  private static final Map<String, String> CONDITIONS = Map.of(
    "New",
    "NEW",
    "Like new",
    "LIKE_NEW",
    "Good",
    "GOOD",
    "Fair",
    "FAIR",
    "Needs repair",
    "DAMAGED"
  );

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
      conditionBox.getItems().setAll(CONDITIONS.keySet());
      conditionBox.setValue("Good");
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
    headingLabel.setText("Edit listing");
    mainCategoryBox.setDisable(true);
    subcategoryBox.setDisable(true);
    titleField.setText(ad.path("title").asText());
    priceField.setText(ad.path("price").asText());
    descriptionArea.setText(ad.path("description").asText());
    attributesArea.setText(ad.path("attributesText").asText());
    for (JsonNode image : ad.path("imageUrls"))
      imageList.getItems().add(image.asText());
    CONDITIONS.entrySet()
      .stream()
      .filter(x -> x.getValue().equals(ad.path("itemCondition").asText()))
      .map(Map.Entry::getKey)
      .findFirst()
      .ifPresent(conditionBox::setValue);
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
      subcategoryChanged();
    }
    cityBox
      .getItems()
      .stream()
      .filter(x -> x.id() == ad.path("cityId").asLong())
      .findFirst()
      .ifPresent(cityBox::setValue);
  }

  @FXML
  private void subcategoryChanged() {
    dynamicAttributesBox.getChildren().clear();
    attributeInputs.clear();
    attributeLabels.clear();
    Option category = subcategoryBox.getValue();
    if (category == null) {
      dynamicAttributesBox.setVisible(false);
      dynamicAttributesBox.setManaged(false);
      return;
    }
    safe(() -> {
      JsonNode definitions = api.get(
        "/api/categories/" + category.id() + "/attributes"
      );
      dynamicAttributesBox.setVisible(!definitions.isEmpty());
      dynamicAttributesBox.setManaged(!definitions.isEmpty());
      if (!definitions.isEmpty()) {
        Label heading = new Label("Item specifications");
        heading.getStyleClass().add("section-title");
        dynamicAttributesBox.getChildren().add(heading);
      }
      for (JsonNode definition : definitions) {
        String key = definition.path("key").asText();
        String label = definition.path("label").asText();
        boolean required = definition.path("required").asBoolean();
        Control input;
        if ("SELECT".equals(definition.path("inputType").asText())) {
          ComboBox<String> combo = new ComboBox<>();
          for (JsonNode option : definition.path("options")) {
            combo.getItems().add(option.asText());
          }
          combo.setMaxWidth(Double.MAX_VALUE);
          input = combo;
        } else {
          TextField field = new TextField();
          field.setPromptText("Enter " + label.toLowerCase());
          input = field;
        }
        String oldValue =
          existing == null
            ? ""
            : existing.path("attributes").path(label).asText("");
        setValue(input, oldValue);
        HBox row = new HBox(
          12,
          new Label(label + (required ? " *" : "")),
          input
        );
        row.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(input, Priority.ALWAYS);
        dynamicAttributesBox.getChildren().add(row);
        attributeInputs.put(key, input);
        attributeLabels.put(key, label);
      }
    });
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
  private void chooseImages() {
    safe(() -> {
      FileChooser chooser = new FileChooser();
      chooser.setTitle("Choose listing photos");
      chooser
        .getExtensionFilters()
        .add(
          new FileChooser.ExtensionFilter(
            "Image files",
            "*.jpg",
            "*.jpeg",
            "*.png",
            "*.webp",
            "*.gif"
          )
        );
      var files = chooser.showOpenMultipleDialog(
        imageList.getScene().getWindow()
      );
      if (files == null) return;
      if (
        imageList.getItems().size() + files.size() > 10
      ) throw new ApiClient.ApiException(
        400,
        "A listing can contain at most 10 photos."
      );
      for (var file : files)
        imageList
          .getItems()
          .add(api.uploadImage(file).path("imageUrl").asText());
    });
  }

  @FXML
  private void removeImage() {
    int selected = imageList.getSelectionModel().getSelectedIndex();
    if (selected >= 0) imageList.getItems().remove(selected);
  }

  @FXML
  private void submit() {
    safe(() -> {
      errorLabel.setText("");
      if (
        titleField.getText().isBlank() ||
        descriptionArea.getText().isBlank() ||
        priceField.getText().isBlank() ||
        subcategoryBox.getValue() == null ||
        cityBox.getValue() == null ||
        conditionBox.getValue() == null
      ) {
        errorLabel.setText("Complete all required fields.");
        return;
      }
      try {
        if (
          new java.math.BigDecimal(priceField.getText().trim()).signum() < 0
        ) {
          errorLabel.setText("Price must be a non-negative number.");
          return;
        }
      } catch (NumberFormatException e) {
        errorLabel.setText("Price must be a non-negative number.");
        return;
      }
      Map<String, Object> body = new LinkedHashMap<>();
      body.put("title", titleField.getText());
      body.put("description", descriptionArea.getText());
      body.put("price", priceField.getText());
      body.put("categoryId", subcategoryBox.getValue().id());
      body.put("cityId", cityBox.getValue().id());
      body.put("itemCondition", CONDITIONS.get(conditionBox.getValue()));
      body.put("attributesText", attributesArea.getText());
      Map<String, String> attributes = new LinkedHashMap<>();
      attributeInputs.forEach((key, input) ->
        attributes.put(key, value(input))
      );
      body.put("attributes", attributes);
      body.put("imageUrls", new ArrayList<>(imageList.getItems()));
      if (existing == null) api.post("/api/ads", body);
      else api.put("/api/ads/" + existing.path("id").asLong(), body);
      DialogUtils.info("Listing saved. It will appear after admin review.");
      NavigationManager.myAds();
    });
  }

  private String value(Control input) {
    if (input instanceof TextInputControl text) return text.getText();
    if (input instanceof ComboBox<?> combo) {
      return combo.getValue() == null ? "" : combo.getValue().toString();
    }
    return "";
  }

  private void setValue(Control input, String value) {
    if (value == null || value.isBlank()) return;
    if (input instanceof TextInputControl text) text.setText(value);
    else if (input instanceof ComboBox<?> combo) {
      int index = combo.getItems().indexOf(value);
      if (index >= 0) combo.getSelectionModel().select(index);
    }
  }

  @FXML
  private void cancel() {
    NavigationManager.myAds();
  }
}
