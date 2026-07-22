package com.secondhand.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(
    GlobalExceptionHandler.class
  );

  record ErrorBody(
    String message,
    int status,
    Instant timestamp,
    String path
  ) {}

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ErrorBody> api(ApiException e, HttpServletRequest r) {
    return ResponseEntity.status(e.status).body(
      new ErrorBody(
        e.getMessage(),
        e.status.value(),
        Instant.now(),
        r.getRequestURI()
      )
    );
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    MultipartException.class,
  })
  ResponseEntity<ErrorBody> malformed(Exception e, HttpServletRequest r) {
    return ResponseEntity.badRequest().body(
      new ErrorBody("Malformed request", 400, Instant.now(), r.getRequestURI())
    );
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<ErrorBody> uploadTooLarge(
    MaxUploadSizeExceededException e,
    HttpServletRequest r
  ) {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
      new ErrorBody(
        "Image must be smaller than 8 MB",
        HttpStatus.PAYLOAD_TOO_LARGE.value(),
        Instant.now(),
        r.getRequestURI()
      )
    );
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<ErrorBody> conflict(
    DataIntegrityViolationException e,
    HttpServletRequest r
  ) {
    return ResponseEntity.status(HttpStatus.CONFLICT).body(
      new ErrorBody(
        "A record with the same value already exists",
        HttpStatus.CONFLICT.value(),
        Instant.now(),
        r.getRequestURI()
      )
    );
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorBody> validation(
    MethodArgumentNotValidException e,
    HttpServletRequest r
  ) {
    String m = e
      .getBindingResult()
      .getFieldErrors()
      .stream()
      .map(x -> x.getField() + ": " + x.getDefaultMessage())
      .findFirst()
      .orElse("Validation failed");
    return ResponseEntity.badRequest().body(
      new ErrorBody(m, 400, Instant.now(), r.getRequestURI())
    );
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ErrorBody> constraint(
    ConstraintViolationException e,
    HttpServletRequest r
  ) {
    String message = e
      .getConstraintViolations()
      .stream()
      .map(x -> x.getMessage())
      .findFirst()
      .orElse("Validation failed");
    return ResponseEntity.badRequest().body(
      new ErrorBody(message, 400, Instant.now(), r.getRequestURI())
    );
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorBody> other(Exception e, HttpServletRequest r) {
    if (e instanceof ErrorResponse error) {
      int status = error.getStatusCode().value();
      String message = switch (status) {
        case 404 -> "Resource not found";
        case 405 -> "Request method not supported";
        case 415 -> "Content type not supported";
        default -> "Invalid request";
      };
      return ResponseEntity.status(status).body(
        new ErrorBody(message, status, Instant.now(), r.getRequestURI())
      );
    }
    log.error("Unexpected error while handling {}", r.getRequestURI(), e);
    return ResponseEntity.internalServerError().body(
      new ErrorBody(
        "Unexpected server error",
        500,
        Instant.now(),
        r.getRequestURI()
      )
    );
  }
}
