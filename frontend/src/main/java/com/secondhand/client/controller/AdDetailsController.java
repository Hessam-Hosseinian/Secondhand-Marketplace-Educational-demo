package com.secondhand.client.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.secondhand.client.app.NavigationManager;
import com.secondhand.client.auth.SessionManager;
import com.secondhand.client.util.DialogUtils;
import java.util.Map;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;

public class AdDetailsController extends BaseController {
 @FXML private ImageView imageView; @FXML private Label titleLabel,priceLabel,metaLabel,conditionLabel,sellerLabel,ratingLabel,dateLabel; @FXML private Label descriptionLabel,attributesLabel; @FXML private Button favoriteButton,messageButton,ratingButton; 
 private JsonNode ad; private boolean adminMode;
 public void load(long id,boolean admin){adminMode=admin;safe(()->{ad=api.get((admin?"/api/admin/ads/":"/api/ads/")+id);titleLabel.setText(ad.path("title").asText());priceLabel.setText(UiFactory.price(ad));metaLabel.setText(ad.path("mainCategoryName").asText()+" / "+ad.path("categoryName").asText()+"  ·  "+ad.path("cityName").asText());conditionLabel.setText(ad.path("itemCondition").asText().replace('_',' '));sellerLabel.setText(ad.path("ownerName").asText());double rating=ad.path("sellerAverageRating").asDouble(0);ratingLabel.setText(rating==0?"No ratings yet":"★ "+String.format("%.1f",rating));dateLabel.setText(ad.path("createdAt").asText().replace('T',' '));descriptionLabel.setText(ad.path("description").asText());attributesLabel.setText(ad.path("attributesText").asText(""));imageView.setImage(UiFactory.image(UiFactory.firstImage(ad),720,390).getImage());boolean own=ad.path("ownerId").asLong()==SessionManager.userId();favoriteButton.setDisable(own||admin);messageButton.setDisable(own||admin);ratingButton.setDisable(own||admin);});}
 @FXML private void back(){if(adminMode)NavigationManager.admin();else NavigationManager.mainAds();}
 @FXML private void favorite(){safe(()->{api.post("/api/favorites/"+ad.path("id").asLong(),Map.of());DialogUtils.info("Saved to your favorites.");favoriteButton.setDisable(true);});}
 @FXML private void message(){safe(()->{JsonNode c=api.post("/api/conversations?adId="+ad.path("id").asLong(),Map.of());NavigationManager.chat(c.path("id").asLong());});}
 @FXML private void rate(){NavigationManager.rating(ad);}
}
