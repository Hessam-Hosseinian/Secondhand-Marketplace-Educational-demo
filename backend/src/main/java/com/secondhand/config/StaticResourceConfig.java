package com.secondhand.config;
import java.nio.file.Paths;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
@Configuration public class StaticResourceConfig implements WebMvcConfigurer {
 private final String location;
 public StaticResourceConfig(@Value("${app.upload.dir:uploads}")String path){location=Paths.get(path).toAbsolutePath().normalize().toUri().toString();}
 @Override public void addResourceHandlers(ResourceHandlerRegistry registry){registry.addResourceHandler("/uploads/**").addResourceLocations(location);}
}
