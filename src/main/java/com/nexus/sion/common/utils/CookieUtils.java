package com.nexus.sion.common.utils;

import java.time.Duration;

import org.springframework.http.ResponseCookie;

public class CookieUtils {

  private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

  private CookieUtils() {}

  public static ResponseCookie createRefreshTokenCookie(String refreshToken) {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .httpOnly(true)
        // .secure(true) // HTTPS에서만
        .path("/")
        .maxAge(Duration.ofDays(7))
        .sameSite("Strict")
        .build();
  }

  public static ResponseCookie createDeleteRefreshTokenCookie() {
    return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
        .httpOnly(true)
        // .secure(true)
        .path("/")
        .maxAge(0)
        .sameSite("Strict")
        .build();
  }
}
