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
  ALREADY_DELETED_USER("20013", "이미 삭제된 구성원입니다.", HttpStatus.BAD_REQUEST),
  CANNOT_DELETE_ADMIN("20014", "관리자는 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST),
  INVALID_SORT_COLUMN("20015", "유효하지 않는 정렬 기준입니다", HttpStatus.BAD_REQUEST),
  INVALID_GRADE("20016", "유효하지 않는 등급 정보입니다. ", HttpStatus.BAD_REQUEST),
  FIRST_MIN_YEARS_SHOULD_BE_1("20015", "첫 번째 구간의 minYears는 1이어야 합니다.", HttpStatus.BAD_REQUEST),
  LAST_MAX_YEARS_SHOULD_BE_NULL("20016", "마지막 구간의 maxYears는 null이어야 합니다.", HttpStatus.BAD_REQUEST),
  INTERVAL_YEARS_SHOULD_BE_CONTINUOUS("20017", "구간은 연속되어야 합니다.", HttpStatus.BAD_REQUEST),

  // project
  PROJECT_CODE_DUPLICATED("30001", "이미 존재하는 프로젝트 코드입니다.", HttpStatus.CONFLICT),
  PROJECT_NOT_FOUND("30002", "해당 프로젝트가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  DOMAIN_NOT_FOUND("30003", "해당 도메인이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  JOB_NOT_FOUND("30004", "해당 직무가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  INVALID_CLIENT_CODE_FORMAT("30005", "유효하지 않은 고객사 코드입니다.", HttpStatus.NOT_FOUND),
  CLIENT_COMPANY_ALREADY_EXIST("30006", "이미 존재하는 고객사 코드입니다.", HttpStatus.CONFLICT),
  DOMAIN_ALREADY_EXIST("30007", "이미 존재하는 도메인입니다.", HttpStatus.CONFLICT),
  JOB_ALREADY_EXIST("30008", "이미 존재하는 직무입니다.", HttpStatus.CONFLICT),
  CLIENT_COMPANY_NOT_FOUND("30009", "해당 고객사가 존재하지 않습니다.", HttpStatus.NOT_FOUND),

  // squad
  PROJECT_SQUAD_NOT_FOUND("40001", "해당 프로젝트에 스쿼드가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
  SQUAD_DETAIL_NOT_FOUND("40002", "스쿼드 상세 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
  COMMENT_CONTENT_EMPTY("40003", "코멘트 내용(content)은 공백일 수 없습니다.", HttpStatus.BAD_REQUEST),
  PROJECT_CODE_INVALID("40004", "유효하지 않은 프로젝트 코드입니다.", HttpStatus.BAD_REQUEST),
  COMMENT_NOT_FOUND("40005", "존재하지 않는 코멘트입니다.", HttpStatus.NOT_FOUND),
  INVALID_COMMENT_ACCESS("40006", "해당 스쿼드의 코멘트가 아닙니다.", HttpStatus.FORBIDDEN),
  INVALID_SQUAD_PROJECT_CODE_FORMAT(
      "40007", "프로젝트 코드 형식이 올바르지 않습니다. 예: ha_1_1", HttpStatus.BAD_REQUEST),
  SQUAD_ASSIGNMENT_FAILED("40008", "스쿼드 구성 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
  SQUAD_NOT_FOUND("40009", "해당 스쿼드를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

  // techstack
  TECH_STACK_NOT_FOUND("50001", "해당 기술스택을 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
  TECH_STACK_ALREADY_EXIST("50002", "이미 존재하는 기술스택입니다.", HttpStatus.CONFLICT),

  // statistics
  INVALID_PERIOD("60001", "지원하지 않는 기간 값입니다. (1m, 6m, 1y, 5y 중 선택)", HttpStatus.BAD_REQUEST);

  // evaluation

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}
