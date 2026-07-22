package com.secondhand.controller;

import com.secondhand.exception.ApiException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/images")
public class ImageController {

  private static final Map<String, String> EXTENSIONS = Map.of(
    "image/jpeg",
    ".jpg",
    "image/png",
    ".png",
    "image/webp",
    ".webp",
    "image/gif",
    ".gif"
  );
  private final Path directory;

  public ImageController(@Value("${app.upload.dir:uploads}") String value)
    throws IOException {
    directory = Paths.get(value).toAbsolutePath().normalize();
    Files.createDirectories(directory);
  }

  @PostMapping(
    value = "/upload",
    consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public ResponseEntity<Map<String, String>> upload(
    @RequestPart("file") MultipartFile file
  ) throws IOException {
    if (file.isEmpty()) throw ApiException.bad("Select a non-empty image");
    if (file.getSize() > 8 * 1024 * 1024) throw ApiException.bad(
      "Image must be smaller than 8 MB"
    );
    String extension = EXTENSIONS.get(file.getContentType());
    if (extension == null) throw ApiException.bad(
      "Supported formats: JPG, PNG, WebP and GIF"
    );
    String name = UUID.randomUUID() + extension;
    Files.copy(
      file.getInputStream(),
      directory.resolve(name),
      StandardCopyOption.REPLACE_EXISTING
    );
    String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
      .path("/uploads/")
      .path(name)
      .toUriString();
    return ResponseEntity.status(201).body(Map.of("imageUrl", imageUrl));
  }
}
