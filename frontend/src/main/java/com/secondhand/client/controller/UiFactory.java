package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import java.text.DecimalFormat;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;

final class UiFactory {
 private UiFactory(){}
 static ImageView image(String url,double w,double h){
  ImageView view=new ImageView();view.setFitWidth(w);view.setFitHeight(h);view.setPreserveRatio(false);view.getStyleClass().add("ad-image");
  if(url!=null&&!url.isBlank()){Image image=new Image(url,true);view.setImage(image);image.errorProperty().addListener((o,a,b)->{if(b)view.setImage(null);});}
  return view;
 }
 static String firstImage(JsonNode ad){return ad.path("imageUrls").size()>0?ad.path("imageUrls").get(0).asText():null;}
 static String price(JsonNode ad){return new DecimalFormat("#,###").format(ad.path("price").asDouble())+" IRR";}
 static Region spacer(){Region r=new Region();HBox.setHgrow(r,Priority.ALWAYS);return r;}
 static HBox row(){HBox row=new HBox(14);row.setAlignment(Pos.CENTER_LEFT);row.getStyleClass().add("list-row");return row;}
 static Button action(String text,Runnable action){Button b=new Button(text);b.setOnAction(e->action.run());return b;}
 static Label title(String value){Label label=new Label(value);label.getStyleClass().add("card-title");return label;}
}
