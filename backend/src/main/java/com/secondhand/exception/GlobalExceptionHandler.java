package com.secondhand.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorBody> other(Exception e, HttpServletRequest r) {
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
