package com.secondhand.seed;

import com.secondhand.entity.*;
import com.secondhand.entity.account.*;
import com.secondhand.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.secondhand.service.*;

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
  private final CategoryAttributeRepository categoryAttributes;
  private final CategoryFactory categoryFactory;
  private final ProductFactory productFactory;
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
    CategoryAttributeRepository categoryAttributes,
    CategoryFactory categoryFactory,
    ProductFactory productFactory,
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
    this.categoryAttributes = categoryAttributes;
    this.categoryFactory = categoryFactory;
    this.productFactory = productFactory;
    this.encoder = encoder;
  }

  @Override
  @Transactional
  public void run(String... args) {
    if (users.count() > 0) {
      ensureCatalog();
      credentials();
      return;
    }
    seed();
    ensureCatalog();
  }

  private void ensureCatalog() {
    categories.findAll().forEach(category -> {
      Category expected = categoryFactory.create(
        category.getName(),
        category.getParent()
      );
      category.setCategoryKind(expected.categoryType());
    });
    ads.findAll().forEach(ad -> {
      Advertisement expected = productFactory.create(ad.getCategory(), Map.of());
      ad.setProductKind(expected.productType());
    });

    Category home = ensureCategory("خانه و آشپزخانه", null);
    Category furniture = ensureCategory("مبلمان", home);
    Category appliance = ensureCategory("لوازم خانگی", home);
    Category fashion = ensureCategory("مد و پوشاک", null);
    Category clothing = ensureCategory("لباس", fashion);
    Category bag = ensureCategory("کیف و کفش", fashion);
    Category leisure = ensureCategory("ورزش و سرگرمی", null);
    Category bicycle = ensureCategory("دوچرخه", leisure);
    Category console = ensureCategory("کنسول بازی", leisure);
    Category tools = ensureCategory("ابزار و تجهیزات", null);
    Category industrial = ensureCategory("ابزار صنعتی", tools);
    Category culture = ensureCategory("کتاب و آموزش", null);
    Category book = ensureCategory("کتاب", culture);

    addAttribute(furniture, "material", "جنس", "SELECT", "چوب|فلز|پارچه|ترکیبی", true, 1);
    addAttribute(furniture, "color", "رنگ", "TEXT", null, true, 2);
    addAttribute(appliance, "brand", "برند", "TEXT", null, true, 1);
    addAttribute(appliance, "energy", "رده مصرف انرژی", "SELECT", "A+++|A++|A+|A|B|C", false, 2);
    addAttribute(clothing, "size", "سایز", "SELECT", "XS|S|M|L|XL|XXL", true, 1);
    addAttribute(clothing, "color", "رنگ", "TEXT", null, false, 2);
    addAttribute(bag, "material", "جنس", "TEXT", null, false, 1);
    addAttribute(bicycle, "frameSize", "اندازه فریم", "TEXT", null, true, 1);
    addAttribute(console, "brand", "سازنده", "SELECT", "Sony|Microsoft|Nintendo|سایر", true, 1);
    addAttribute(console, "storage", "حافظه", "TEXT", null, false, 2);
    addAttribute(industrial, "brand", "برند", "TEXT", null, false, 1);
    addAttribute(industrial, "power", "توان", "TEXT", null, false, 2);
    addAttribute(book, "author", "نویسنده", "TEXT", null, true, 1);
    addAttribute(book, "publisher", "ناشر", "TEXT", null, false, 2);

    categories.findByName("ماشین").ifPresent(c -> {
      addAttribute(c, "brand", "برند", "TEXT", null, true, 1);
      addAttribute(c, "modelYear", "سال ساخت", "TEXT", null, true, 2);
      addAttribute(c, "mileage", "کارکرد", "TEXT", null, false, 3);
    });
    categories.findByName("گوشی").ifPresent(c -> {
      addAttribute(c, "brand", "برند", "TEXT", null, true, 1);
      addAttribute(c, "storage", "حافظه", "SELECT", "۶۴ گیگ|۱۲۸ گیگ|۲۵۶ گیگ|۵۱۲ گیگ|۱ ترابایت", true, 2);
      addAttribute(c, "ram", "رم", "TEXT", null, false, 3);
    });
    categories.findByName("لپ‌تاپ").ifPresent(c -> {
      addAttribute(c, "brand", "برند", "TEXT", null, true, 1);
      addAttribute(c, "ram", "رم", "TEXT", null, true, 2);
      addAttribute(c, "storage", "حافظه", "TEXT", null, true, 3);
    });
  }

  private Category ensureCategory(String name, Category parent) {
    return categories.findByName(name).orElseGet(() -> category(name, parent));
  }

  private void addAttribute(
    Category category,
    String key,
    String label,
    String type,
    String options,
    boolean required,
    int order
  ) {
    if (categoryAttributes.existsByCategoryIdAndAttributeKey(category.getId(), key)) return;
    CategoryAttribute attribute = new CategoryAttribute();
    attribute.setCategory(category);
    attribute.setAttributeKey(key);
    attribute.setLabel(label);
    attribute.setInputType(type);
    attribute.setOptionsText(options);
    attribute.setRequired(required);
    attribute.setSortOrder(order);
    categoryAttributes.save(attribute);
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
      Advertisement ad = productFactory.create(
        categoryMap.get(items[i][1]),
        Map.of()
      );
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
    User user = role == Enums.Role.ADMIN
      ? new AdminUser()
      : new CustomerUser();
    user.setFullName(fullName);
    user.setUsername(username);
    user.setPasswordHash(encoder.encode(password));
    user.setPhoneNumber("09" + String.format("%09d", Math.abs(username.hashCode()) % 1_000_000_000L));
    user.setEmail(username + "@secondhand.local");
    user.setRole(role);
    user.setStatus(status);
    return users.save(user);
  }

  private Category category(String name, Category parent) {
    Category category = categoryFactory.create(name, parent);
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
