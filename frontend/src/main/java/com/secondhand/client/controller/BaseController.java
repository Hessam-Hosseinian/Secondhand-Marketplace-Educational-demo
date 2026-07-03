package com.secondhand.client.controller;

import com.secondhand.client.api.ApiClient;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.auth.SessionManager;
import com.secondhand.client.util.DialogUtils;

public abstract class BaseController {

  protected final ApiClient api = new ApiClient();

  protected void safe(Runnable action) {
    try {
      action.run();
    } catch (Exception e) {
      DialogUtils.error(e);
      if (!SessionManager.loggedIn()) NavigationManager.login();
    }
  }
}
