package com.secondhand.client.util;

import javafx.scene.control.*;

public final class DialogUtils {

  private DialogUtils() {}

  public static void info(String m) {
    show(Alert.AlertType.INFORMATION, "Marketplace", m);
  }

  public static void error(Throwable e) {
    show(Alert.AlertType.ERROR, "Something went wrong", e.getMessage());
  }

  private static void show(Alert.AlertType t, String h, String m) {
    Alert a = new Alert(t);
    a.setTitle(h);
    a.setHeaderText(null);
    a.setContentText(m);
    a.showAndWait();
  }
}
