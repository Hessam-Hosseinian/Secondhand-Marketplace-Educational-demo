package com.secondhand.client.controller;

import com.secondhand.client.api.ApiClient;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.util.DialogUtils;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.application.Platform;

public abstract class BaseController {

  protected final ApiClient api = new ApiClient();

  protected void safe(Runnable action) {
    try {
      action.run();
    } catch (Exception e) {
      DialogUtils.error(e);
      if (
        e instanceof ApiClient.ApiException apiError &&
        apiError.statusCode() == 401
      ) NavigationManager.login();
    }
  }

  protected <T> void async(
    Supplier<T> action,
    Consumer<T> onSuccess,
    Runnable onFinished
  ) {
    CompletableFuture.supplyAsync(action).whenComplete((result, error) ->
      Platform.runLater(() -> {
        try {
          safe(() -> {
            if (error != null) {
              Throwable cause = error instanceof CompletionException
                ? error.getCause()
                : error;
              if (cause instanceof RuntimeException runtime) throw runtime;
              throw new IllegalStateException(cause);
            }
            onSuccess.accept(result);
          });
        } finally {
          onFinished.run();
        }
      })
    );
  }
}
