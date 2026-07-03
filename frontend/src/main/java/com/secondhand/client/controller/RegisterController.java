package com.secondhand.client.controller;

import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.auth.SessionManager;
import java.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController extends BaseController {

  @FXML
  private TextField fullNameField, usernameField, phoneField;

  @FXML
  private PasswordField passwordField;

  @FXML
  private Label errorLabel;

  @FXML
  private void submit() {
    safe(() -> {
      errorLabel.setText("");
      if (
        fullNameField.getText().isBlank() ||
        usernameField.getText().isBlank() ||
        passwordField.getText().length() < 6
      ) {
        errorLabel.setText(
          "Complete the required fields; password must have 6 characters."
        );
        return;
      }
      Map<String, String> body = new HashMap<>();
      body.put("fullName", fullNameField.getText());
      body.put("username", usernameField.getText());
      body.put("password", passwordField.getText());
      body.put("phoneNumber", phoneField.getText());
      SessionManager.login(api.post("/api/auth/register", body));
      NavigationManager.mainAds();
    });
  }

  @FXML
  private void back() {
    NavigationManager.login();
  }
}
