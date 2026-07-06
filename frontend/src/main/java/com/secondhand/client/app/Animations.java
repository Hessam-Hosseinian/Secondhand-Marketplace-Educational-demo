package com.secondhand.client.app;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Small collection of reusable, tasteful animations used across the app.
 * All helpers are null-safe and side-effect only, so they can be sprinkled
 * onto any node without changing layout logic.
 */
public final class Animations {

  private Animations() {}

  /** Soft fade + slight upward drift used when a new scene is shown. */
  public static void enterScene(Node root) {
    if (root == null) return;
    root.setOpacity(0);
    root.setTranslateY(14);

    FadeTransition fade = new FadeTransition(Duration.millis(420), root);
    fade.setFromValue(0);
    fade.setToValue(1);

    TranslateTransition slide = new TranslateTransition(
      Duration.millis(420),
      root
    );
    slide.setFromY(14);
    slide.setToY(0);
    slide.setInterpolator(Interpolator.SPLINE(0.16, 1, 0.3, 1));

    new ParallelTransition(fade, slide).play();
  }

  /** Fade + rise for a single node, with an optional start delay. */
  public static void fadeInUp(Node node, double delayMs) {
    if (node == null) return;
    node.setOpacity(0);
    node.setTranslateY(18);

    FadeTransition fade = new FadeTransition(Duration.millis(460), node);
    fade.setFromValue(0);
    fade.setToValue(1);

    TranslateTransition slide = new TranslateTransition(
      Duration.millis(460),
      node
    );
    slide.setFromY(18);
    slide.setToY(0);
    slide.setInterpolator(Interpolator.SPLINE(0.16, 1, 0.3, 1));

    ParallelTransition group = new ParallelTransition(node, fade, slide);
    group.setDelay(Duration.millis(delayMs));
    group.play();
  }

  /** Staggered entrance for a list/grid of nodes (cards, rows, etc.). */
  public static void stagger(Iterable<? extends Node> nodes) {
    if (nodes == null) return;
    int index = 0;
    for (Node node : nodes) {
      fadeInUp(node, Math.min(index * 55.0, 500));
      index++;
    }
  }

  /**
   * Adds a subtle lift on hover: the node scales up slightly and gains a
   * softer, deeper shadow. Great for cards and list rows.
   */
  public static void hoverLift(Node node) {
    if (node == null) return;

    DropShadow shadow = new DropShadow();
    shadow.setColor(Color.rgb(60, 40, 30, 0.22));
    shadow.setRadius(0);
    shadow.setOffsetY(0);

    ScaleTransition grow = new ScaleTransition(Duration.millis(180), node);
    grow.setToX(1.025);
    grow.setToY(1.025);

    ScaleTransition shrink = new ScaleTransition(Duration.millis(180), node);
    shrink.setToX(1);
    shrink.setToY(1);

    node.setOnMouseEntered(e -> {
      node.setEffect(shadow);
      Timeline in = new Timeline(
        new KeyFrame(
          Duration.millis(200),
          new KeyValue(shadow.radiusProperty(), 26),
          new KeyValue(shadow.offsetYProperty(), 14)
        )
      );
      shrink.stop();
      grow.playFromStart();
      in.play();
    });

    node.setOnMouseExited(e -> {
      Timeline out = new Timeline(
        new KeyFrame(
          Duration.millis(200),
          new KeyValue(shadow.radiusProperty(), 0),
          new KeyValue(shadow.offsetYProperty(), 0)
        )
      );
      out.setOnFinished(done -> node.setEffect(null));
      grow.stop();
      shrink.playFromStart();
      out.play();
    });
  }

  /** Quick press feedback: dips scale down then springs back. */
  public static void pressPop(Node node) {
    if (node == null) return;
    ScaleTransition down = new ScaleTransition(Duration.millis(90), node);
    down.setToX(0.96);
    down.setToY(0.96);

    ScaleTransition up = new ScaleTransition(Duration.millis(140), node);
    up.setToX(1);
    up.setToY(1);
    up.setInterpolator(Interpolator.SPLINE(0.34, 1.56, 0.64, 1));

    new SequentialTransition(down, up).play();
  }
}
