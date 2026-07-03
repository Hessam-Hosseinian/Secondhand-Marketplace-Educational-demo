package com.secondhand.controller;

import com.secondhand.exception.ApiException;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/images")
public class ImageController {

  private static final Set<String> ALLOWED = Set.of(
    "image/jpeg",
    "image/png",
    "image/webp",
    "image/gif"
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
    if (!ALLOWED.contains(file.getContentType())) throw ApiException.bad(
      "Supported formats: JPG, PNG, WebP and GIF"
    );
    String original = Objects.toString(file.getOriginalFilename(), "image");
    String extension = original.contains(".")
      ? original.substring(original.lastIndexOf('.')).toLowerCase()
      : "";
    String name = UUID.randomUUID() + extension;
    Files.copy(
      file.getInputStream(),
      directory.resolve(name),
      StandardCopyOption.REPLACE_EXISTING
    );
    return ResponseEntity.status(201).body(
      Map.of("imageUrl", "http://localhost:8080/uploads/" + name)
    );
  }
}
