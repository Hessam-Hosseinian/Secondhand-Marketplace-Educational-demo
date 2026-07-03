package com.secondhand.repository;

import com.secondhand.entity.AdminReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminReviewRepository
  extends JpaRepository<AdminReview, Long> {}
