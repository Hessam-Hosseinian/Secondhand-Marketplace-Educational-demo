package com.secondhand.client.controller;

import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.auth.SessionManager;
import java.util.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class RegisterController extends BaseController {

  @FXML
  private TextField fullNameField, usernameField, phoneField, emailField;

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
        emailField.getText().isBlank() ||
        phoneField.getText().isBlank() ||
        passwordField.getText().length() < 6
      ) {
        errorLabel.setText(
          "Complete every field. Password must contain at least 6 characters."
        );
        return;
      }
      Map<String, String> body = new HashMap<>();
      body.put("fullName", fullNameField.getText());
      body.put("username", usernameField.getText());
      body.put("password", passwordField.getText());
      body.put("phoneNumber", phoneField.getText());
      body.put("email", emailField.getText());
      SessionManager.login(api.post("/api/auth/register", body));
      NavigationManager.mainAds();
    });
  }

  @FXML
  private void back() {
    NavigationManager.login();
  }
}
