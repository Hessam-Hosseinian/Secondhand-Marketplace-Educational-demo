package com.secondhand.repository;

import com.secondhand.entity.*;
import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface ConversationRepository
  extends JpaRepository<Conversation, Long>
{
  Optional<Conversation> findByBuyerAndAdvertisement(User b, Advertisement a);

  @Query(
    "select c from Conversation c where c.buyer=?1 or c.seller=?1 order by c.createdAt desc"
  )
  List<Conversation> forUser(User u);

  boolean existsByAdvertisementAndBuyerAndSeller(
    Advertisement a,
    User b,
    User s
  );
}
