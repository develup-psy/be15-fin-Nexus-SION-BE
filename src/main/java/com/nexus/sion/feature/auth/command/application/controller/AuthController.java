package com.nexus.sion.feature.auth.command.application.controller;

import static com.nexus.sion.common.utils.CookieUtils.createDeleteRefreshTokenCookie;
import static com.nexus.sion.common.utils.CookieUtils.createRefreshTokenCookie;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.common.dto.ApiResponse;
import com.nexus.sion.feature.auth.command.application.dto.request.LoginRequest;
import com.nexus.sion.feature.auth.command.application.dto.request.RefreshTokenRequest;
import com.nexus.sion.feature.auth.command.application.dto.response.AccessTokenResponse;
import com.nexus.sion.feature.auth.command.application.dto.response.TokenResponse;
import com.nexus.sion.feature.auth.command.application.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
@Tag(name = "", description = "")
@Slf4j
public class AuthController {
  private final AuthService authService;

  /* 테스트 로그인 */
  @Operation(summary = "테스트용 로그인", description = "테스트용 로그인 후 JWT를 발급합니다.")
  @PostMapping("/login/test")
  public ResponseEntity<ApiResponse<AccessTokenResponse>> login() {
    TokenResponse token = authService.testLogin();
    log.info(token.toString());
    return buildTokenResponse(token);
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AccessTokenResponse>> login(
      @RequestBody @Valid LoginRequest loginRequest) {
    TokenResponse tokenResponse = authService.login(loginRequest);
    return buildTokenResponse(tokenResponse);
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<AccessTokenResponse>> refreshToken(
      @RequestBody @Valid RefreshTokenRequest request) {
    TokenResponse response = authService.refreshToken(request.getRefreshToken());
    return buildTokenResponse(response);
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(@RequestBody @Valid RefreshTokenRequest request) {
    authService.logout(request.getRefreshToken());

    ResponseCookie deleteCookie = createDeleteRefreshTokenCookie(); // 만료용 쿠키 생성

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
        .body(ApiResponse.success(null));
  }

  /* accessToken 과 refreshToken을 body와 쿠키에 담아 반환 */
  private ResponseEntity<ApiResponse<AccessTokenResponse>> buildTokenResponse(
      TokenResponse tokenResponse) {
    ResponseCookie cookie =
        createRefreshTokenCookie(tokenResponse.getRefreshToken()); // refreshToken
    // 쿠키 생성
    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, cookie.toString())
        .body(
            ApiResponse.success(
                AccessTokenResponse.builder().accessToken(tokenResponse.getAccessToken()).build()));
  }
}
