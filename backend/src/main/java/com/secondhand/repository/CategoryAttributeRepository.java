package com.secondhand.repository;

import com.secondhand.entity.CategoryAttribute;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryAttributeRepository extends JpaRepository<CategoryAttribute, Long> {
  List<CategoryAttribute> findByCategoryIdOrderBySortOrder(Long categoryId);
  boolean existsByCategoryIdAndAttributeKey(Long categoryId, String attributeKey);
}
