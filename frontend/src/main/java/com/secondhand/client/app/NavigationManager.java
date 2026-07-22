package com.secondhand.client.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.controller.*;
import com.secondhand.client.auth.SessionManager;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.geometry.NodeOrientation;
import javafx.stage.*;

public final class NavigationManager {

  private static Stage stage;

  private NavigationManager() {}

  public static void initialize(Stage value) {
    stage = value;
    stage.setTitle("Re:Market");
    stage.setMinWidth(1040);
    stage.setMinHeight(720);
  }

  public static void login() {
    show("login.fxml", null);
  }

  public static void register() {
    show("register.fxml", null);
  }

  public static void mainAds() {
    show("main_ads.fxml", null);
  }

  public static void details(long id) {
    show("ad_details.fxml", (AdDetailsController c) -> c.load(id, false));
  }

  public static void adminDetails(long id) {
    show("ad_details.fxml", (AdDetailsController c) -> c.load(id, true));
  }

  public static void adForm(JsonNode ad) {
    if (!requireLogin()) return;
    show("create_edit_ad.fxml", (CreateEditAdController c) -> c.load(ad));
  }

  public static void myAds() {
    if (!requireLogin()) return;
    show("my_ads.fxml", null);
  }

  public static void favorites() {
    if (!requireLogin()) return;
    show("favorites.fxml", null);
  }

  public static void conversations() {
    if (!requireLogin()) return;
    show("conversations.fxml", null);
  }

  public static void chat(long id) {
    if (!requireLogin()) return;
    show("chat.fxml", (ChatController c) -> c.load(id));
  }

  public static void admin() {
    if (!requireLogin()) return;
    show("admin_panel.fxml", null);
  }

  public static void rating(JsonNode ad) {
    if (!requireLogin()) return;
    try {
      FXMLLoader loader = loader("rating_dialog.fxml");
      Parent root = loader.load();
      RatingDialogController controller = loader.getController();
      Stage dialog = new Stage();
      controller.initializeDialog(dialog, ad);
      dialog.initOwner(stage);
      dialog.initModality(Modality.WINDOW_MODAL);
      dialog.setTitle("Rate seller");
      Scene scene = new Scene(root);
      scene.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
      scene.getStylesheets().add(css());
      dialog.setScene(scene);
      dialog.showAndWait();
    } catch (IOException e) {
      throw new IllegalStateException("Could not open rating dialog", e);
    }
  }

  public static boolean requireLogin() {
    if (SessionManager.loggedIn()) return true;
    login();
    return false;
  }

  private static <T> void show(String file, Consumer<T> setup) {
    try {
      FXMLLoader loader = loader(file);
      Parent root = loader.load();
      if (setup != null) setup.accept(loader.getController());
      Scene scene = new Scene(root, 1200, 800);
      scene.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
      scene.getStylesheets().add(css());
      stage.setScene(scene);
    } catch (IOException e) {
      throw new IllegalStateException("Could not load " + file, e);
    }
  }

  private static FXMLLoader loader(String file) {
    return new FXMLLoader(NavigationManager.class.getResource("/fxml/" + file));
  }

  private static String css() {
    return Objects.requireNonNull(
      NavigationManager.class.getResource("/styles/app.css")
    ).toExternalForm();
  }
}
