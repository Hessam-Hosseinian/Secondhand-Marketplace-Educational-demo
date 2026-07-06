package com.secondhand.client.controller;

import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.auth.SessionManager;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController extends BaseController {

  @FXML
  private TextField usernameField;

  @FXML
  private PasswordField passwordField;

  @FXML
  private Label errorLabel;

  @FXML
  private void login() {
    safe(() -> {
      errorLabel.setText("");
      if (
        usernameField.getText().isBlank() || passwordField.getText().isBlank()
      ) {
        errorLabel.setText("Enter your username and password.");
        return;
      }
      SessionManager.login(
        api.post(
          "/api/auth/login",
          Map.of(
            "username",
            usernameField.getText(),
            "password",
            passwordField.getText()
          )
        )
      );
      NavigationManager.mainAds();
    });
  }

  @FXML
  private void register() {
    NavigationManager.register();
  }

  @FXML
  private void browse() {
    NavigationManager.mainAds();
  }
}
