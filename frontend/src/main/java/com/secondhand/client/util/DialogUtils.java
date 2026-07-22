package com.secondhand.client.util;

import java.util.Objects;
import java.util.Optional;
import javafx.scene.control.*;

public final class DialogUtils {

  private DialogUtils() {}

  public static void info(String m) {
    show(Alert.AlertType.INFORMATION, "Marketplace", m);
  }

  public static void error(Throwable e) {
    String message = e.getMessage() == null
      ? "An unexpected error occurred. Please try again."
      : e.getMessage();
    show(Alert.AlertType.ERROR, "Something went wrong", message);
  }

  public static boolean confirm(String message) {
    Alert alert = alert(Alert.AlertType.CONFIRMATION, "Please confirm", message);
    Optional<ButtonType> result = alert.showAndWait();
    return result.isPresent() && result.get() == ButtonType.OK;
  }

  public static Optional<String> prompt(
    String title,
    String header,
    String placeholder
  ) {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle(title);
    dialog.setHeaderText(header);
    dialog.setContentText(null);
    TextField editor = dialog.getEditor();
    editor.setPromptText(placeholder);
    style(dialog.getDialogPane());
    return dialog.showAndWait().map(String::trim).filter(value -> !value.isBlank());
  }

  private static void show(Alert.AlertType t, String h, String m) {
    alert(t, h, m).showAndWait();
  }

  private static Alert alert(Alert.AlertType type, String title, String message) {
    Alert alert = new Alert(type);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    style(alert.getDialogPane());
    return alert;
  }

  private static void style(DialogPane pane) {
    pane
      .getStylesheets()
      .add(
        Objects.requireNonNull(
          DialogUtils.class.getResource("/styles/app.css")
        ).toExternalForm()
      );
    pane.getStyleClass().add("app-dialog");
  }
}
