package com.secondhand.client.app;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/** Reusable hover effects used across the app. */
public final class Animations {

  private Animations() {}

  /** Adds a subtle scale and shadow while the pointer is over a node. */
  public static void hoverLift(Node node) {
    if (node == null) return;

    DropShadow shadow = new DropShadow();
    shadow.setColor(Color.rgb(2, 132, 199, 0.24));
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
}
