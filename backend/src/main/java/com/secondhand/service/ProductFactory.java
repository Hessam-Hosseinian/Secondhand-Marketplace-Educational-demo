package com.secondhand.service;

import com.secondhand.entity.*;
import com.secondhand.entity.product.*;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class ProductFactory {
  public Advertisement create(Category category, Map<String, String> values) {
    Advertisement product = switch (category.getName()) {
      case "ماشین" -> car(values);
      case "موتور" -> motorcycle(values);
      case "گوشی" -> phone(values);
      case "لپ‌تاپ" -> laptop(values);
      case "مبلمان" -> furniture(values);
      case "لوازم خانگی" -> appliance(values);
      case "لباس" -> clothing(values);
      case "کیف و کفش" -> bagShoe(values);
      case "دوچرخه" -> bicycle(values);
      case "کنسول بازی" -> console(values);
      case "ابزار صنعتی" -> tool(values);
      case "کتاب" -> book(values);
      default -> new Advertisement();
    };
    product.setProductKind(product.productType());
    return product;
  }

  private CarAdvertisement car(Map<String, String> v) {
    CarAdvertisement p = new CarAdvertisement();
    p.setBrand(value(v, "brand"));
    p.setModelYear(integer(v, "modelYear"));
    p.setMileage(longValue(v, "mileage"));
    return p;
  }

  private MotorcycleAdvertisement motorcycle(Map<String, String> v) {
    MotorcycleAdvertisement p = new MotorcycleAdvertisement();
    p.setBrand(value(v, "brand"));
    p.setModelYear(integer(v, "modelYear"));
    p.setEngineCapacity(integer(v, "engineCapacity"));
    p.setMileage(longValue(v, "mileage"));
    return p;
  }

  private PhoneAdvertisement phone(Map<String, String> v) {
    PhoneAdvertisement p = new PhoneAdvertisement();
    p.setBrand(value(v, "brand"));
    p.setStorage(value(v, "storage"));
    p.setRam(value(v, "ram"));
    p.setBatteryHealth(integer(v, "batteryHealth"));
    return p;
  }

  private LaptopAdvertisement laptop(Map<String, String> v) {
    LaptopAdvertisement p = new LaptopAdvertisement();
    p.setBrand(value(v, "brand"));
    p.setRam(value(v, "ram"));
    p.setStorage(value(v, "storage"));
    p.setCpu(value(v, "cpu"));
    p.setGpu(value(v, "gpu"));
    return p;
  }

  private FurnitureAdvertisement furniture(Map<String, String> v) {
    FurnitureAdvertisement p = new FurnitureAdvertisement();
    p.setMaterial(value(v, "material"));
    p.setColor(value(v, "color"));
    p.setDimensions(value(v, "dimensions"));
    p.setPieceCount(integer(v, "pieceCount"));
    return p;
  }

  private HomeApplianceAdvertisement appliance(Map<String, String> v) {
    HomeApplianceAdvertisement p = new HomeApplianceAdvertisement();
    p.setBrand(value(v, "brand"));
    p.setEnergyRating(value(v, "energy"));
    p.setCapacity(value(v, "capacity"));
    p.setWarrantyMonths(integer(v, "warrantyMonths"));
    return p;
  }

  private ClothingAdvertisement clothing(Map<String, String> v) {
    ClothingAdvertisement p = new ClothingAdvertisement();
    p.setSize(value(v, "size"));
    p.setColor(value(v, "color"));
    p.setMaterial(value(v, "material"));
    p.setTargetGender(value(v, "targetGender"));
    return p;
  }

  private BagShoeAdvertisement bagShoe(Map<String, String> v) {
    BagShoeAdvertisement p = new BagShoeAdvertisement();
    p.setBrand(value(v, "brand"));
    p.setMaterial(value(v, "material"));
    p.setSize(value(v, "size"));
    p.setColor(value(v, "color"));
    return p;
  }

  private BicycleAdvertisement bicycle(Map<String, String> v) {
    BicycleAdvertisement p = new BicycleAdvertisement();
    p.setBrand(value(v, "brand"));
    p.setFrameSize(value(v, "frameSize"));
    p.setWheelSize(value(v, "wheelSize"));
    p.setGearCount(integer(v, "gearCount"));
    return p;
  }

  private GameConsoleAdvertisement console(Map<String, String> v) {
    GameConsoleAdvertisement p = new GameConsoleAdvertisement();
    p.setManufacturer(value(v, "brand"));
    p.setStorage(value(v, "storage"));
    p.setControllerCount(integer(v, "controllerCount"));
    p.setRegion(value(v, "region"));
    return p;
  }

  private IndustrialToolAdvertisement tool(Map<String, String> v) {
    IndustrialToolAdvertisement p = new IndustrialToolAdvertisement();
    p.setBrand(value(v, "brand"));
    p.setPower(value(v, "power"));
    p.setVoltage(value(v, "voltage"));
    p.setSafetyClass(value(v, "safetyClass"));
    return p;
  }

  private BookAdvertisement book(Map<String, String> v) {
    BookAdvertisement p = new BookAdvertisement();
    p.setAuthor(value(v, "author"));
    p.setPublisher(value(v, "publisher"));
    p.setIsbn(value(v, "isbn"));
    p.setPageCount(integer(v, "pageCount"));
    p.setLanguage(value(v, "language"));
    return p;
  }

  private String value(Map<String, String> values, String key) {
    return values == null ? null : values.get(key);
  }

  private Integer integer(Map<String, String> values, String key) {
    try {
      String value = value(values, key);
      return value == null || value.isBlank() ? null : Integer.valueOf(value);
    } catch (NumberFormatException ignored) {
      return null;
    }
  }

  private Long longValue(Map<String, String> values, String key) {
    try {
      String value = value(values, key);
      return value == null || value.isBlank() ? null : Long.valueOf(value);
    } catch (NumberFormatException ignored) {
      return null;
    }
  }
}
