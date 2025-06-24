package com.nexus.sion.exception;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public enum ErrorCode {
  // auth
  VALIDATION_ERROR("10001", "입력 값 검증 오류입니다.", HttpStatus.BAD_REQUEST),
  INTERNAL_SERVER_ERROR("10002", "내부 서버 오류입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  UNAUTHORIZED_USER("10003", "인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
  EXPIRED_JWT("10004", "JWT 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
  INVALID_JWT("10005", "잘못된 JWT 토큰입니다.", HttpStatus.UNAUTHORIZED),
  UNSUPPORTED_JWT("10006", "지원하지 않는 JWT 토큰입니다.", HttpStatus.UNAUTHORIZED),
  EMPTY_JWT("10007", "JWT 클레임이 비어있습니다.", HttpStatus.UNAUTHORIZED),
  FORBIDDEN("10008", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

  // member
  USER_NOT_FOUND("20001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  USER_INFO_NOT_FOUND("20002", "정보 조회에 실패했습니다.", HttpStatus.NOT_FOUND),
  ALREADY_REGISTERED_EMAIL("20003", "이미 가입한 이메일입니다.", HttpStatus.CONFLICT),
  ALREADY_REGISTERED_EMPLOYEE_IDENTIFICATION_NUMBER("20004", "이미 가입한 사번입니다.", HttpStatus.CONFLICT),
  INVALID_PASSWORD_FORMAT("20004", "최소 8자, 영문자, 숫자, 특수문자 포함해야합니다.", HttpStatus.BAD_REQUEST),
  INVALID_PHONE_NUMBER_FORMAT(
      "20005", "휴대폰 번호는 하이픈(-) 없이 10자리 또는 11자리 숫자로 입력해주세요. 예: 01012345678", HttpStatus.BAD_REQUEST),
  INVALID_EMAIL_FORMAT(
      "20006", "이메일 형식이 올바르지 않습니다. 예: example@example.com", HttpStatus.BAD_REQUEST),
  INVALID_CURRENT_PASSWORD("20007", "비밀번호가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
  INVALID_MEMBER_STATUS("20008", "유효하지 않은 상태값 입니다.", HttpStatus.BAD_REQUEST),
  POSITION_NOT_FOUND("20010", "존재하지 않는 직책입니다.", HttpStatus.BAD_REQUEST),
  DEPARTMENT_NOT_FOUND("20011", "존재하지 않는 부서입니다.", HttpStatus.BAD_REQUEST),
  INVALID_BIRTHDAY("20012", "생일이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),


  // project

  // squad
  SQUAD_NOT_FOUND("40001", "해당 프로젝트에 스쿼드가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  PROJECT_CODE_INVALID("40002", "유효하지 않은 프로젝트 코드입니다.", HttpStatus.BAD_REQUEST);

  // techstack

  // statistics

  // evaluation

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
