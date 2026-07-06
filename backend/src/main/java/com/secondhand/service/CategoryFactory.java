package com.secondhand.service;

import com.secondhand.entity.*;
import com.secondhand.entity.category.*;
import org.springframework.stereotype.Component;

@Component
public class CategoryFactory {
  public Category create(String name, Category parent) {
    Category category = switch (name) {
      case "وسایل نقلیه" -> new VehicleCategory();
      case "ماشین" -> new CarCategory();
      case "موتور" -> new MotorcycleCategory();
      case "وسایل الکترونیکی" -> new ElectronicsCategory();
      case "گوشی" -> new PhoneCategory();
      case "لپ‌تاپ" -> new LaptopCategory();
      case "خانه و آشپزخانه" -> new HomeCategory();
      case "مبلمان" -> new FurnitureCategory();
      case "لوازم خانگی" -> new HomeApplianceCategory();
      case "مد و پوشاک" -> new FashionCategory();
      case "لباس" -> new ClothingCategory();
      case "کیف و کفش" -> new BagShoeCategory();
      case "ورزش و سرگرمی" -> new LeisureCategory();
      case "دوچرخه" -> new BicycleCategory();
      case "کنسول بازی" -> new GameConsoleCategory();
      case "ابزار و تجهیزات" -> new ToolsCategory();
      case "ابزار صنعتی" -> new IndustrialToolCategory();
      case "کتاب و آموزش" -> new CultureCategory();
      case "کتاب" -> new BookCategory();
      default -> new Category();
    };
    category.setName(name);
    category.setParent(parent);
    category.setCategoryKind(category.categoryType());
    return category;
  }
}
