package com.secondhand.repository;

import com.secondhand.entity.*;
import java.math.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface AdvertisementRepository
  extends JpaRepository<Advertisement, Long>
{
  List<Advertisement> findByOwnerOrderByCreatedAtDesc(User owner);
  List<Advertisement> findByStatusOrderByCreatedAtDesc(Enums.AdStatus status);

  @Query(
    "select distinct a from Advertisement a where a.status='ACTIVE' and (:keyword is null or lower(a.title) like lower(concat('%',:keyword,'%')) or lower(a.description) like lower(concat('%',:keyword,'%'))) and (:cityId is null or a.city.id=:cityId) and (:categoryId is null or a.category.id=:categoryId or a.category.parent.id=:categoryId) and (:minPrice is null or a.price>=:minPrice) and (:maxPrice is null or a.price<=:maxPrice) and (:condition is null or a.itemCondition=:condition)"
  )
  List<Advertisement> search(
    @Param("keyword") String keyword,
    @Param("categoryId") Long categoryId,
    @Param("cityId") Long cityId,
    @Param("minPrice") BigDecimal minPrice,
    @Param("maxPrice") BigDecimal maxPrice,
    @Param("condition") Enums.ItemCondition condition,
    org.springframework.data.domain.Sort sort
  );
}
