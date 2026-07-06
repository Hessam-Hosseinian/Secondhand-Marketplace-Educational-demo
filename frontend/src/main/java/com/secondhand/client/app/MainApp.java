package com.secondhand.client.app;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

  @Override
  public void start(Stage stage) {
    NavigationManager.initialize(stage);
    NavigationManager.mainAds();
    stage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
