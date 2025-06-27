package com.nexus.sion.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.nexus.sion.common.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** 비즈니스 예외 처리 */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn("BusinessException 발생: {} - {}", errorCode.getCode(), errorCode.getMessage(), e);
    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), errorCode.getMessage());
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  /** 유효성 검사 예외 처리 */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationException(
      MethodArgumentNotValidException e) {
    ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
    StringBuilder errorMessage = new StringBuilder(errorCode.getMessage());
    for (FieldError error : e.getBindingResult().getFieldErrors()) {
      errorMessage.append(String.format(" [%s: %s]", error.getField(), error.getDefaultMessage()));
    }
    log.warn("Validation 실패: {}", errorMessage.toString());
    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), errorMessage.toString());
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  /** 예상치 못한 예외 처리 */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    log.error("Unhandled 예외 발생: {}", e.getMessage(), e);
    String[] errorSplit = e.getClass().toString().split("\\.");
    ApiResponse<Void> response =
        ApiResponse.failure(errorSplit[errorSplit.length - 1], e.getMessage());
            ApiResponse.failure(errorSplit[errorSplit.length - 1], e.getMessage());
    String errorClass = e.getClass().getSimpleName();
    ;
    ApiResponse<Void> response = ApiResponse.failure(errorClass, e.getMessage());
    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  /** 인증 실패 (잘못된 자격 증명) */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadCredentials() {
    ErrorCode errorCode = ErrorCode.UNAUTHORIZED_USER;
    log.warn("BadCredentialsException 발생: 인증 실패");
    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), errorCode.getMessage());
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  /** 인가 실패 (권한 없음) */
  @ExceptionHandler(AuthorizationDeniedException.class)
  public ResponseEntity<ApiResponse<Void>> handleAccessDenied() {
    ErrorCode errorCode = ErrorCode.FORBIDDEN;
    log.warn("AuthorizationDeniedException 발생: 권한 없음");
    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), errorCode.getMessage());
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }

  /** JWT 관련 예외 처리 */
  @ExceptionHandler(CustomJwtException.class)
  public ResponseEntity<ApiResponse<Void>> handleJwtException(CustomJwtException e) {
    ErrorCode errorCode = e.getErrorCode();
    log.warn("JWT 예외 발생: {} - {}", errorCode.getCode(), errorCode.getMessage(), e);
    ApiResponse<Void> response = ApiResponse.failure(errorCode.getCode(), errorCode.getMessage());
    return new ResponseEntity<>(response, errorCode.getHttpStatus());
  }
}
