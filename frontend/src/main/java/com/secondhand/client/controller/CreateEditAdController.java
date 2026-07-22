package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.api.ApiClient;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.model.Option;
import com.secondhand.client.util.DialogUtils;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

public class CreateEditAdController extends BaseController {

  @FXML
  private Label headingLabel, formSubtitleLabel, titleCountLabel,
    descriptionCountLabel, imageCountLabel, photoEmptyLabel, errorLabel,
    saveStatusLabel;

  @FXML
  private TextField titleField, priceField;

  @FXML
  private TextArea descriptionArea, attributesArea;

  @FXML
  private ComboBox<Option> mainCategoryBox, subcategoryBox, cityBox;

  @FXML
  private ComboBox<String> conditionBox;

  @FXML
  private VBox dynamicAttributesBox, formLoading;

  @FXML
  private ListView<String> imageList;

  @FXML
  private ScrollPane formScroll;

  @FXML
  private Button uploadButton, removeImageButton, submitButton;

  @FXML
  private ProgressIndicator imageUploadIndicator;

  private JsonNode existing;
  private boolean metadataLoaded;
  private boolean existingPopulated;
  private boolean uploading;
  private boolean submitting;
  private boolean attributesLoading;
  private int attributeRequestVersion;
  private final List<Option> categories = new ArrayList<>();
  private final Map<String, Control> attributeInputs = new LinkedHashMap<>();
  private final Map<String, String> attributeLabels = new LinkedHashMap<>();
  private final Set<String> requiredAttributes = new LinkedHashSet<>();

  private static final List<String> CONDITION_LABELS = List.of(
    "New",
    "Like new",
    "Good",
    "Fair",
    "Needs repair"
  );

  private static final Map<String, String> CONDITIONS = Map.of(
    "New", "NEW",
    "Like new", "LIKE_NEW",
    "Good", "GOOD",
    "Fair", "FAIR",
    "Needs repair", "DAMAGED"
  );

  private record FormOptions(JsonNode categories, JsonNode cities) {}

  @FXML
  private void initialize() {
    configureForm();
    setFormLoading(true);
    async(
      () -> new FormOptions(api.get("/api/categories"), api.get("/api/cities")),
      this::applyOptions,
      () -> setFormLoading(false)
    );
  }

  private void configureForm() {
    conditionBox.getItems().setAll(CONDITION_LABELS);
    conditionBox.setValue("Good");
    limit(titleField, 255);
    limit(descriptionArea, 5000);
    limit(attributesArea, 3000);
    titleField.textProperty().addListener((observable, oldValue, value) ->
      titleCountLabel.setText(value.length() + " / 255")
    );
    descriptionArea.textProperty().addListener((observable, oldValue, value) ->
      descriptionCountLabel.setText(value.length() + " / 5000")
    );
    imageList.setOrientation(Orientation.HORIZONTAL);
    imageList.setCellFactory(list -> new PhotoCell());
    imageList.getItems().addListener(
      (javafx.collections.ListChangeListener<String>) change -> updatePhotoState()
    );
    imageList.getSelectionModel().selectedItemProperty().addListener(
      (observable, oldValue, value) -> removeImageButton.setDisable(value == null)
    );
    updatePhotoState();
  }

  private void applyOptions(FormOptions options) {
    categories.clear();
    for (JsonNode node : options.categories()) {
      categories.add(
        new Option(
          node.path("id").asLong(),
          node.path("name").asText(),
          node.path("parentId").isNull() ? null : node.path("parentId").asLong()
        )
      );
    }
    mainCategoryBox.getItems().setAll(
      categories.stream().filter(option -> option.parentId() == null).toList()
    );
    cityBox.getItems().clear();
    for (JsonNode node : options.cities()) {
      cityBox.getItems().add(
        new Option(node.path("id").asLong(), node.path("name").asText(), null)
      );
    }
    metadataLoaded = true;
    if (!mainCategoryBox.getItems().isEmpty()) {
      mainCategoryBox.getSelectionModel().selectFirst();
    }
    if (!cityBox.getItems().isEmpty()) cityBox.getSelectionModel().selectFirst();
    mainChanged();
    populateExisting();
  }

  public void load(JsonNode ad) {
    existing = ad;
    if (ad == null) return;
    headingLabel.setText("Edit your listing");
    formSubtitleLabel.setText(
      "Update the details below. Edited listings return to the review queue."
    );
    submitButton.setText("Save changes");
    saveStatusLabel.setText("Changes will be reviewed before publishing again.");
    populateExisting();
  }

  private void populateExisting() {
    if (existing == null || !metadataLoaded || existingPopulated) return;
    existingPopulated = true;
    titleField.setText(existing.path("title").asText());
    priceField.setText(existing.path("price").asText());
    descriptionArea.setText(existing.path("description").asText());
    attributesArea.setText(existing.path("attributesText").asText());
    imageList.getItems().clear();
    for (JsonNode image : existing.path("imageUrls")) {
      if (!image.asText().isBlank()) imageList.getItems().add(image.asText());
    }
    CONDITIONS
      .entrySet()
      .stream()
      .filter(entry -> entry.getValue().equals(existing.path("itemCondition").asText()))
      .map(Map.Entry::getKey)
      .findFirst()
      .ifPresent(conditionBox::setValue);

    Option child = categories
      .stream()
      .filter(option -> option.id() == existing.path("categoryId").asLong())
      .findFirst()
      .orElse(null);
    if (child != null) {
      categories
        .stream()
        .filter(option -> Objects.equals(option.id(), child.parentId()))
        .findFirst()
        .ifPresent(mainCategoryBox::setValue);
      mainChanged();
      subcategoryBox.setValue(child);
      subcategoryChanged();
    }
    cityBox
      .getItems()
      .stream()
      .filter(option -> option.id() == existing.path("cityId").asLong())
      .findFirst()
      .ifPresent(cityBox::setValue);
    mainCategoryBox.setDisable(true);
    subcategoryBox.setDisable(true);
  }

  @FXML
  private void mainChanged() {
    subcategoryBox.getItems().clear();
    Option main = mainCategoryBox.getValue();
    if (main != null) {
      subcategoryBox.getItems().addAll(
        categories
          .stream()
          .filter(option -> Objects.equals(option.parentId(), main.id()))
          .toList()
      );
    }
    if (!subcategoryBox.getItems().isEmpty()) {
      subcategoryBox.getSelectionModel().selectFirst();
    } else {
      subcategoryChanged();
    }
  }

  @FXML
  private void subcategoryChanged() {
    int requestVersion = ++attributeRequestVersion;
    dynamicAttributesBox.getChildren().clear();
    attributeInputs.clear();
    attributeLabels.clear();
    requiredAttributes.clear();
    Option category = subcategoryBox.getValue();
    if (category == null) {
      attributesLoading = false;
      updateActionAvailability();
      dynamicAttributesBox.setVisible(false);
      dynamicAttributesBox.setManaged(false);
      return;
    }

    dynamicAttributesBox.setVisible(true);
    dynamicAttributesBox.setManaged(true);
    attributesLoading = true;
    updateActionAvailability();
    Label loading = new Label("Loading category specifications…");
    loading.getStyleClass().add("muted");
    dynamicAttributesBox.getChildren().add(loading);
    async(
      () -> api.get("/api/categories/" + category.id() + "/attributes"),
      definitions -> {
        if (requestVersion == attributeRequestVersion) renderAttributes(definitions);
      },
      () -> {
        if (requestVersion == attributeRequestVersion) {
          attributesLoading = false;
          updateActionAvailability();
        }
      }
    );
  }

  private void renderAttributes(JsonNode definitions) {
    dynamicAttributesBox.getChildren().clear();
    dynamicAttributesBox.setVisible(!definitions.isEmpty());
    dynamicAttributesBox.setManaged(!definitions.isEmpty());
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
      String oldValue = existing == null
        ? ""
        : existing.path("attributes").path(label).asText("");
      setValue(input, oldValue);
      Label fieldLabel = new Label(label + (required ? "  *" : ""));
      fieldLabel.getStyleClass().add("field-label");
      VBox field = new VBox(6, fieldLabel, input);
      field.getStyleClass().add("specification-field");
      dynamicAttributesBox.getChildren().add(field);
      attributeInputs.put(key, input);
      attributeLabels.put(key, label);
      if (required) requiredAttributes.add(key);
    }
  }

  @FXML
  private void chooseImages() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle("Choose listing photos");
    chooser.getExtensionFilters().add(
      new FileChooser.ExtensionFilter(
        "Image files",
        "*.jpg", "*.jpeg", "*.png", "*.webp", "*.gif"
      )
    );
    List<File> files = chooser.showOpenMultipleDialog(imageList.getScene().getWindow());
    if (files == null || files.isEmpty()) return;
    if (imageList.getItems().size() + files.size() > 10) {
      DialogUtils.error(
        new ApiClient.ApiException(400, "A listing can contain at most 10 photos.")
      );
      return;
    }
    setUploading(true);
    async(
      () -> {
        List<String> urls = new ArrayList<>();
        for (File file : files) {
          urls.add(api.uploadImage(file).path("imageUrl").asText());
        }
        return urls;
      },
      urls -> {
        imageList.getItems().addAll(urls);
        if (!urls.isEmpty()) imageList.getSelectionModel().selectLast();
      },
      () -> setUploading(false)
    );
  }

  @FXML
  private void removeImage() {
    int selected = imageList.getSelectionModel().getSelectedIndex();
    if (selected >= 0) {
      imageList.getItems().remove(selected);
      imageList.refresh();
    }
  }

  @FXML
  private void submit() {
    errorLabel.setText("");
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
    String error = validateForm();
    if (error != null) {
      errorLabel.setText(error);
      errorLabel.setVisible(true);
      errorLabel.setManaged(true);
      return;
    }

    Map<String, Object> body = buildBody();
    setSubmitting(true);
    async(
      () -> {
        if (existing == null) api.post("/api/ads", body);
        else api.put("/api/ads/" + existing.path("id").asLong(), body);
        return true;
      },
      ignored -> {
        DialogUtils.info(
          existing == null
            ? "Listing submitted for review."
            : "Changes saved and submitted for review."
        );
        NavigationManager.myAds();
      },
      () -> setSubmitting(false)
    );
  }

  private String validateForm() {
    if (titleField.getText().isBlank()) return "Add a clear listing title.";
    if (descriptionArea.getText().isBlank()) return "Describe the item and its condition.";
    if (priceField.getText().isBlank()) return "Enter an asking price.";
    if (subcategoryBox.getValue() == null) return "Choose a category and subcategory.";
    if (cityBox.getValue() == null) return "Choose the pickup city.";
    if (conditionBox.getValue() == null) return "Choose the item condition.";
    try {
      if (new BigDecimal(priceField.getText().trim()).signum() < 0) {
        return "Price must be a non-negative number.";
      }
    } catch (NumberFormatException error) {
      return "Price must be a non-negative number.";
    }
    for (String key : requiredAttributes) {
      if (value(attributeInputs.get(key)).isBlank()) {
        return "Complete the required “" + attributeLabels.get(key) + "” field.";
      }
    }
    return null;
  }

  private Map<String, Object> buildBody() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("title", titleField.getText().trim());
    body.put("description", descriptionArea.getText().trim());
    body.put("price", priceField.getText().trim());
    body.put("categoryId", subcategoryBox.getValue().id());
    body.put("cityId", cityBox.getValue().id());
    body.put("itemCondition", CONDITIONS.get(conditionBox.getValue()));
    body.put("attributesText", attributesArea.getText().trim());
    Map<String, String> attributes = new LinkedHashMap<>();
    attributeInputs.forEach((key, input) -> attributes.put(key, value(input).trim()));
    body.put("attributes", attributes);
    body.put("imageUrls", new ArrayList<>(imageList.getItems()));
    return body;
  }

  private void setFormLoading(boolean loading) {
    formLoading.setVisible(loading);
    formLoading.setManaged(loading);
    formScroll.setDisable(loading);
    formScroll.setOpacity(loading ? 0.3 : 1);
  }

  private void setUploading(boolean uploading) {
    this.uploading = uploading;
    imageUploadIndicator.setVisible(uploading);
    imageUploadIndicator.setManaged(uploading);
    updateActionAvailability();
    saveStatusLabel.setText(
      uploading ? "Uploading photos…" : defaultSaveStatus()
    );
  }

  private void setSubmitting(boolean submitting) {
    this.submitting = submitting;
    updateActionAvailability();
    submitButton.setText(
      submitting ? "Saving…" : (existing == null ? "Submit for review" : "Save changes")
    );
    saveStatusLabel.setText(
      submitting ? "Securely saving your listing…" : defaultSaveStatus()
    );
  }

  private String defaultSaveStatus() {
    return existing == null
      ? "Your listing will be reviewed before it goes live."
      : "Changes will be reviewed before publishing again.";
  }

  private void updatePhotoState() {
    int count = imageList.getItems().size();
    imageCountLabel.setText(count + " / 10 photos");
    photoEmptyLabel.setVisible(count == 0);
    photoEmptyLabel.setManaged(count == 0);
    imageList.setVisible(count > 0);
    imageList.setManaged(count > 0);
    removeImageButton.setDisable(imageList.getSelectionModel().getSelectedItem() == null);
    updateActionAvailability();
  }

  private void updateActionAvailability() {
    uploadButton.setDisable(
      uploading || submitting || imageList.getItems().size() >= 10
    );
    submitButton.setDisable(uploading || submitting || attributesLoading);
  }

  private void limit(TextInputControl input, int maximum) {
    input.setTextFormatter(
      new TextFormatter<String>(change ->
        change.getControlNewText().length() <= maximum ? change : null
      )
    );
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

  private final class PhotoCell extends ListCell<String> {
    @Override
    protected void updateItem(String url, boolean empty) {
      super.updateItem(url, empty);
      if (empty || url == null) {
        setGraphic(null);
        return;
      }
      ImageView preview = UiFactory.image(url, 146, 96);
      Label position = new Label(getIndex() == 0 ? "Cover photo" : "Photo " + (getIndex() + 1));
      position.getStyleClass().add(getIndex() == 0 ? "cover-photo-label" : "photo-position-label");
      Button remove = new Button("×");
      remove.getStyleClass().add("photo-remove-button");
      remove.setTooltip(new Tooltip("Remove this photo"));
      remove.setOnAction(event -> {
        imageList.getItems().remove(url);
        imageList.refresh();
      });
      HBox footer = new HBox(6, position, UiFactory.spacer(), remove);
      footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
      VBox tile = new VBox(7, preview, footer);
      tile.getStyleClass().add("photo-preview-tile");
      setGraphic(tile);
    }
  }

  @FXML
  private void cancel() {
    NavigationManager.myAds();
  }
}
