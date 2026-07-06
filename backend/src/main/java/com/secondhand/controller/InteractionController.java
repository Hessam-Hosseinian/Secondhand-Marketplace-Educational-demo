package com.secondhand.controller;

import com.secondhand.dto.ApiDtos.*;
import com.secondhand.entity.*;
import com.secondhand.exception.ApiException;
import com.secondhand.repository.*;
import com.secondhand.service.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class InteractionController {

  private final CurrentUser current;
  private final AdvertisementRepository ads;
  private final FavoriteRepository favorites;
  private final ConversationRepository conversations;
  private final MessageRepository messages;
  private final SellerRatingRepository ratings;
  private final UserRepository users;
  private final MapperService mapper;

  public InteractionController(
    CurrentUser c,
    AdvertisementRepository a,
    FavoriteRepository f,
    ConversationRepository co,
    MessageRepository m,
    SellerRatingRepository r,
    UserRepository u,
    MapperService mp
  ) {
    current = c;
    ads = a;
    favorites = f;
    conversations = co;
    messages = m;
    ratings = r;
    users = u;
    mapper = mp;
  }

  private Advertisement ad(Long id) {
    return ads
      .findById(id)
      .orElseThrow(() -> ApiException.notFound("Advertisement not found"));
  }

  private Conversation conversation(User u, Long id) {
    Conversation c = conversations
      .findById(id)
      .orElseThrow(() -> ApiException.notFound("Conversation not found"));
    if (
      !c.getBuyer().getId().equals(u.getId()) &&
      !c.getSeller().getId().equals(u.getId())
    ) throw ApiException.forbidden("Not a conversation participant");
    return c;
  }

  @GetMapping("/favorites")
  @Transactional(readOnly = true)
  public List<AdDto> favorites(Authentication a) {
    return favorites
      .findByUserOrderByCreatedAtDesc(current.get(a))
      .stream()
      .map(Favorite::getAdvertisement)
      .filter(x -> x.getStatus() == Enums.AdStatus.ACTIVE)
      .map(mapper::ad)
      .toList();
  }

  @PostMapping("/favorites/{id}")
  @Transactional
  public ResponseEntity<Void> favorite(
    Authentication au,
    @PathVariable Long id
  ) {
    User u = current.get(au);
    Advertisement a = ad(id);
    if (a.getStatus() != Enums.AdStatus.ACTIVE) throw ApiException.bad(
      "Only active ads can be favorited"
    );
    if (
      favorites.findByUserAndAdvertisement(u, a).isPresent()
    ) throw ApiException.conflict("Advertisement already favorited");
    Favorite f = new Favorite();
    f.setUser(u);
    f.setAdvertisement(a);
    favorites.save(f);
    return ResponseEntity.status(201).build();
  }

  @DeleteMapping("/favorites/{id}")
  @Transactional
  public ResponseEntity<Void> unfavorite(
    Authentication au,
    @PathVariable Long id
  ) {
    User u = current.get(au);
    favorites
      .findByUserAndAdvertisement(u, ad(id))
      .ifPresent(favorites::delete);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/conversations")
  @Transactional
  public ConversationDto start(Authentication au, @RequestParam Long adId) {
    User u = current.get(au);
    Advertisement a = ad(adId);
    if (a.getStatus() != Enums.AdStatus.ACTIVE) throw ApiException.bad(
      "Conversation requires an active ad"
    );
    if (a.getOwner().getId().equals(u.getId())) throw ApiException.bad(
      "You cannot message yourself"
    );
    ensureActive(a.getOwner());
    Conversation c = conversations
      .findByBuyerAndAdvertisement(u, a)
      .orElseGet(() -> {
        Conversation n = new Conversation();
        n.setAdvertisement(a);
        n.setBuyer(u);
        n.setSeller(a.getOwner());
        return conversations.save(n);
      });
    return conv(c);
  }

  @GetMapping("/conversations")
  @Transactional(readOnly = true)
  public List<ConversationDto> conversations(Authentication a) {
    return conversations
      .forUser(current.get(a))
      .stream()
      .map(this::conv)
      .toList();
  }

  @GetMapping("/conversations/{id}/messages")
  @Transactional(readOnly = true)
  public List<MessageDto> messages(Authentication a, @PathVariable Long id) {
    return messages
      .findByConversationOrderByCreatedAt(conversation(current.get(a), id))
      .stream()
      .map(this::msg)
      .toList();
  }

  @PostMapping("/conversations/{id}/messages")
  @Transactional
  public ResponseEntity<MessageDto> message(
    Authentication a,
    @PathVariable Long id,
    @Valid @RequestBody MessageRequest r
  ) {
    User u = current.get(a);
    Conversation c = conversation(u, id);
    ensureActive(c.getBuyer());
    ensureActive(c.getSeller());
    Message m = new Message();
    m.setConversation(c);
    m.setSender(u);
    m.setContent(r.content().trim());
    return ResponseEntity.status(201).body(msg(messages.save(m)));
  }

  @PostMapping("/ratings")
  @Transactional
  public ResponseEntity<RatingDto> rate(
    Authentication au,
    @Valid @RequestBody RatingRequest r
  ) {
    User buyer = current.get(au);
    Advertisement a = ad(r.advertisementId());
    User seller = users
      .findById(r.sellerId())
      .orElseThrow(() -> ApiException.notFound("Seller not found"));
    if (buyer.getId().equals(seller.getId())) throw ApiException.bad(
      "You cannot rate yourself"
    );
    if (!a.getOwner().getId().equals(seller.getId())) throw ApiException.bad(
      "Seller does not own this advertisement"
    );
    if (
      !conversations.existsByAdvertisementAndBuyerAndSeller(a, buyer, seller)
    ) throw ApiException.forbidden(
      "A conversation with the seller is required"
    );
    if (
      ratings.existsByAdvertisementAndBuyerAndSeller(a, buyer, seller)
    ) throw ApiException.conflict("Seller already rated for this ad");
    SellerRating x = new SellerRating();
    x.setAdvertisement(a);
    x.setBuyer(buyer);
    x.setSeller(seller);
    x.setRating(r.rating());
    x.setComment(r.comment());
    ratings.save(x);
    return ResponseEntity.status(201).body(rating(x));
  }

  @GetMapping("/users/{id}/ratings")
  @Transactional(readOnly = true)
  public List<RatingDto> ratings(@PathVariable Long id) {
    User s = users
      .findById(id)
      .orElseThrow(() -> ApiException.notFound("Seller not found"));
    return ratings
      .findBySellerOrderByCreatedAtDesc(s)
      .stream()
      .map(this::rating)
      .toList();
  }

  private ConversationDto conv(Conversation c) {
    Message lastMessage = c.getMessages().isEmpty()
      ? null
      : c.getMessages().get(c.getMessages().size() - 1);
    return new ConversationDto(
      c.getId(),
      c.getAdvertisement().getId(),
      c.getAdvertisement().getTitle(),
      c.getBuyer().getId(),
      c.getBuyer().getFullName(),
      c.getSeller().getId(),
      c.getSeller().getFullName(),
      lastMessage == null ? null : lastMessage.getContent(),
      lastMessage == null ? null : lastMessage.getCreatedAt(),
      c.getCreatedAt()
    );
  }

  private void ensureActive(User user) {
    if (user.getStatus() != Enums.UserStatus.ACTIVE) throw ApiException.forbidden(
      "Messages are unavailable because a participant is blocked"
    );
  }

  private MessageDto msg(Message m) {
    return new MessageDto(
      m.getId(),
      m.getSender().getId(),
      m.getSender().getFullName(),
      m.getContent(),
      m.getCreatedAt()
    );
  }

  private RatingDto rating(SellerRating r) {
    return new RatingDto(
      r.getId(),
      r.getBuyer().getId(),
      r.getBuyer().getFullName(),
      r.getRating(),
      r.getComment(),
      r.getCreatedAt()
    );
  }
}
