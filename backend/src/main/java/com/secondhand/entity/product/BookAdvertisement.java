package com.secondhand.entity.product;
import com.secondhand.entity.*;
import jakarta.persistence.*;
import lombok.*;
@Entity @DiscriminatorValue("BOOK") @Getter @Setter @NoArgsConstructor
public class BookAdvertisement extends Advertisement {
  private String author;
  private String publisher;
  private String isbn;
  private Integer pageCount;
  private String language;
  @Override public String productType() { return "BOOK"; }
}
