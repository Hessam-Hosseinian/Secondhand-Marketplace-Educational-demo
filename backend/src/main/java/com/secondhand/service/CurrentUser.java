package com.secondhand.service;

import com.secondhand.entity.User;
import com.secondhand.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUser {

  public User get(Authentication a) {
    if (
      a == null || !(a.getPrincipal() instanceof User u)
    ) throw new ApiException(
      HttpStatus.UNAUTHORIZED,
      "Authentication required"
    );
    return u;
  }
}
