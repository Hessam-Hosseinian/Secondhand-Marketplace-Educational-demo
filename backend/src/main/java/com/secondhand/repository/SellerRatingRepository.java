package com.secondhand.repository;

import com.secondhand.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface SellerRatingRepository
  extends JpaRepository<SellerRating, Long>
{
  List<SellerRating> findBySellerOrderByCreatedAtDesc(User s);
  boolean existsByAdvertisementAndBuyerAndSeller(
    Advertisement a,
    User b,
    User s
  );

  @Query("select avg(r.rating) from SellerRating r where r.seller=?1")
  Double average(User s);
}
