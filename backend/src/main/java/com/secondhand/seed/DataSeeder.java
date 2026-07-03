package com.secondhand.seed;

import com.secondhand.entity.*;
import com.secondhand.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

  private final UserRepository users;
  private final CategoryRepository categories;
  private final CityRepository cities;
  private final AdvertisementRepository ads;
  private final FavoriteRepository favorites;
  private final ConversationRepository conversations;
  private final MessageRepository messages;
  private final SellerRatingRepository ratings;
  private final PasswordEncoder encoder;

  public DataSeeder(
    UserRepository users,
    CategoryRepository categories,
    CityRepository cities,
    AdvertisementRepository ads,
    FavoriteRepository favorites,
    ConversationRepository conversations,
    MessageRepository messages,
    SellerRatingRepository ratings,
    PasswordEncoder encoder
  ) {
    this.users = users;
    this.categories = categories;
    this.cities = cities;
    this.ads = ads;
    this.favorites = favorites;
    this.conversations = conversations;
    this.messages = messages;
    this.ratings = ratings;
    this.encoder = encoder;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (users.count() > 0) {
      credentials();
      return;
    }
    seed();
  }

  private void seed() {
    user(
      "مدیر سیستم",
      "admin",
      "admin123",
      Enums.Role.ADMIN,
      Enums.UserStatus.ACTIVE
    );
    User seller1 = user(
      "حسام حسینیان",
      "seller1",
      "password123",
      Enums.Role.USER,
      Enums.UserStatus.ACTIVE
    );
    User seller2 = user(
      "رضا کریمی",
      "seller2",
      "password123",
      Enums.Role.USER,
      Enums.UserStatus.ACTIVE
    );
    User buyer1 = user(
      "نیما مرادی",
      "buyer1",
      "password123",
      Enums.Role.USER,
      Enums.UserStatus.ACTIVE
    );
    User buyer2 = user(
      "مینا حسینی",
      "buyer2",
      "password123",
      Enums.Role.USER,
      Enums.UserStatus.ACTIVE
    );
    user(
      "کاربر مسدود",
      "blocked",
      "password123",
      Enums.Role.USER,
      Enums.UserStatus.BLOCKED
    );

    Category vehicle = category("وسایل نقلیه", null);
    Category car = category("ماشین", vehicle);
    Category motorcycle = category("موتور", vehicle);
    Category electronics = category("وسایل الکترونیکی", null);
    Category phone = category("گوشی", electronics);
    Category laptop = category("لپ‌تاپ", electronics);
    Map<String, Category> categoryMap = Map.of(
      "ماشین",
      car,
      "موتور",
      motorcycle,
      "گوشی",
      phone,
      "لپ‌تاپ",
      laptop
    );

    String[] cityNames = { "تهران", "کرج", "اصفهان", "مشهد", "شیراز", "تبریز" };
    List<City> cityList = new ArrayList<>();
    for (String name : cityNames) {
      City city = new City();
      city.setName(name);
      city.setProvince(name);
      cityList.add(cities.save(city));
    }

    String[][] items = {
      {
        "ال نود E2 مدل ۱۳۹۶",
        "ماشین",
        "8500000000",
        "خودروی خانوادگی کم‌استهلاک، موتور و گیربکس سالم، بیمه کامل و بدنه بسیار تمیز.",
      },
      {
        "سمند LX مدل ۱۳۹۹",
        "ماشین",
        "7200000000",
        "سمند شخصی با کارکرد واقعی، سرویس‌ها به‌موقع انجام شده و کابین تمیز است.",
      },
      {
        "پراید ۱۳۱ مدل ۱۳۹۸",
        "ماشین",
        "4600000000",
        "پراید کم‌مصرف و مناسب رفت‌وآمد شهری، فنی سالم و دارای بیمه.",
      },
      {
        "پیکان جوانان کلاسیک",
        "ماشین",
        "2900000000",
        "پیکان جوانان بازسازی‌شده با قطعات اصلی؛ مناسب علاقه‌مندان خودروهای کلاسیک.",
      },
      {
        "تویوتا پرادو دو در",
        "ماشین",
        "95000000000",
        "پرادو دو در سالم و بدون آفرود سنگین، کارشناسی‌شده و آماده انتقال.",
      },
      {
        "موتورسیکلت هوندا ۱۲۵",
        "موتور",
        "2200000000",
        "هوندا ۱۲۵ کم‌کار، انجین بی‌صدا، سند و مدارک کامل.",
      },
      {
        "موتورسیکلت یاماها R25",
        "موتور",
        "7140000000",
        "یاماها R25 اسپرت با سرویس کامل، لاستیک سالم و بدون زمین‌خوردگی.",
      },
      {
        "اسکوتر وسپا پریماورا",
        "موتور",
        "6500000000",
        "وسپا پریماورا بسیار تمیز، مناسب استفاده شهری و دارای لوازم جانبی.",
      },
      {
        "iPhone 17 Pro 256GB",
        "گوشی",
        "2300000000",
        "آیفون ۱۷ پرو رجیسترشده، بدون خط‌وخش، همراه جعبه و کابل اصلی.",
      },
      {
        "Samsung Galaxy S25",
        "گوشی",
        "1250000000",
        "گلکسی S25 با حافظه ۲۵۶ گیگابایت، فعال‌نشده و دارای ضمانت.",
      },
      {
        "iPad 11 نسل جدید",
        "گوشی",
        "620000000",
        "آیپد ۱۱ اینچ بسیار تمیز، مناسب مطالعه و طراحی، همراه شارژر.",
      },
      {
        "Samsung S24 FE",
        "گوشی",
        "850000000",
        "سامسونگ S24 FE دو سیم‌کارت، باتری سالم و بدون تعمیر.",
      },
      {
        "Samsung Galaxy A30",
        "گوشی",
        "120000000",
        "گلکسی A30 کارکرده و سالم، مناسب استفاده روزمره و دانش‌آموزی.",
      },
      {
        "ASUS ROG Strix Gaming",
        "لپ‌تاپ",
        "2500000000",
        "لپ‌تاپ گیمینگ ROG با گرافیک قدرتمند، رم ۳۲ و حافظه SSD یک ترابایت.",
      },
      {
        "ASUS Zenbook 14 OLED",
        "لپ‌تاپ",
        "1700000000",
        "زن‌بوک سبک با نمایشگر OLED، رم ۱۶ و حافظه ۵۱۲ گیگابایت.",
      },
      {
        "MacBook Air M4",
        "لپ‌تاپ",
        "1870000000",
        "مک‌بوک ایر M4 با رم ۱۶ و SSD ۲۵۶، باتری عالی و بدنه بدون ضربه.",
      },
    };

    List<Advertisement> made = new ArrayList<>();
    for (int i = 0; i < items.length; i++) {
      Advertisement ad = new Advertisement();
      ad.setTitle(items[i][0]);
      ad.setDescription(items[i][3]);
      ad.setPrice(new BigDecimal(items[i][2]));
      ad.setCategory(categoryMap.get(items[i][1]));
      ad.setCity(cityList.get(i % cityList.size()));
      ad.setOwner(i % 2 == 0 ? seller1 : seller2);
      ad.setItemCondition(
        i % 4 == 0 ? Enums.ItemCondition.LIKE_NEW : Enums.ItemCondition.GOOD
      );
      ad.setStatus(
        i < 10
          ? Enums.AdStatus.ACTIVE
          : i < 13
            ? Enums.AdStatus.PENDING
            : i < 15
              ? Enums.AdStatus.SOLD
              : Enums.AdStatus.REJECTED
      );
      ad.setAttributesText(attributes(items[i][1], i));
      if (ad.getStatus() == Enums.AdStatus.REJECTED) ad.setRejectionReason(
        "تصویر یا توضیحات آگهی باید کامل‌تر شود."
      );
      AdvertisementImage image = new AdvertisementImage();
      image.setAdvertisement(ad);
      image.setSortOrder(0);
      image.setImageUrl("http://localhost:8080/uploads/seed-" + i + ".jpg");
      ad.getImages().add(image);
      made.add(ads.save(ad));
    }

    favorite(buyer1, made.get(0));
    favorite(buyer1, made.get(8));
    favorite(buyer2, made.get(4));
    favorite(buyer2, made.get(9));
    conversation(
      buyer1,
      made.get(0),
      "سلام، خودرو هنوز موجوده؟",
      "سلام، بله موجوده و امکان بازدید هست.",
      5,
      "فروشنده خوش‌قول بود و خودرو مطابق توضیحات بود."
    );
    conversation(
      buyer2,
      made.get(8),
      "سلام، گوشی رجیستر شده؟",
      "بله، رجیستر و آماده انتقال مالکیت است.",
      4,
      "پاسخ‌گویی سریع و برخورد محترمانه."
    );
    conversation(
      buyer1,
      made.get(6),
      "قیمت موتور کمی قابل مذاکره است؟",
      "بله، بعد از بازدید می‌توانیم صحبت کنیم.",
      5,
      "موتور بسیار تمیز بود."
    );
    credentials();
  }

  private String attributes(String category, int index) {
    return switch (category) {
      case "ماشین" -> "کارکرد تقریبی: " +
        (70 + index * 12) +
        " هزار کیلومتر | سند: تک‌برگ | بیمه: معتبر";
      case "موتور" -> "حجم موتور: " +
        (index == 5 ? "۱۲۵" : "۲۵۰") +
        " سی‌سی | مدارک: کامل";
      case "گوشی" -> "حافظه: ۲۵۶ گیگابایت | وضعیت باتری: سالم | رجیسترشده";
      default -> "رم: ۱۶ گیگابایت | حافظه SSD | همراه شارژر اصلی";
    };
  }

  private User user(
    String fullName,
    String username,
    String password,
    Enums.Role role,
    Enums.UserStatus status
  ) {
    User user = new User();
    user.setFullName(fullName);
    user.setUsername(username);
    user.setPasswordHash(encoder.encode(password));
    user.setPhoneNumber("۰۹۱۲۰۰۰۰۰۰۰");
    user.setRole(role);
    user.setStatus(status);
    return users.save(user);
  }

  private Category category(String name, Category parent) {
    Category category = new Category();
    category.setName(name);
    category.setParent(parent);
    return categories.save(category);
  }

  private void favorite(User user, Advertisement ad) {
    Favorite favorite = new Favorite();
    favorite.setUser(user);
    favorite.setAdvertisement(ad);
    favorites.save(favorite);
  }

  private void conversation(
    User buyer,
    Advertisement ad,
    String buyerText,
    String sellerText,
    int score,
    String comment
  ) {
    Conversation conversation = new Conversation();
    conversation.setAdvertisement(ad);
    conversation.setBuyer(buyer);
    conversation.setSeller(ad.getOwner());
    conversations.save(conversation);
    Message first = new Message();
    first.setConversation(conversation);
    first.setSender(buyer);
    first.setContent(buyerText);
    messages.save(first);
    Message second = new Message();
    second.setConversation(conversation);
    second.setSender(ad.getOwner());
    second.setContent(sellerText);
    messages.save(second);
    SellerRating rating = new SellerRating();
    rating.setAdvertisement(ad);
    rating.setBuyer(buyer);
    rating.setSeller(ad.getOwner());
    rating.setRating(score);
    rating.setComment(comment);
    ratings.save(rating);
  }

  private void credentials() {
    System.out.println(
      "\nSeeded credentials: admin/admin123, seller1/password123, seller2/password123, buyer1/password123, buyer2/password123, blocked/password123\n"
    );
  }
}
