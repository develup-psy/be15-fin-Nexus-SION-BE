package com.nexus.sion.feature.member.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ValidatorUtilTest {

  // -------------------------
  // 이메일 테스트
  // -------------------------
  @Test
  void isEmailValid_실패_골뱅이없음() {
    assertFalse(Validator.isEmailValid("userexample.com")); // @ 없음
  }

  @Test
  void isEmailValid_실패_도메인점없음() {
    assertFalse(Validator.isEmailValid("user@domain")); // . 없음
  }

  @Test
  void isEmailValid_실패_도메인없음() {
    assertFalse(Validator.isEmailValid("user@.com")); // 도메인 이름 없음
  }

  @Test
  void isEmailValid_성공() {
    assertTrue(Validator.isEmailValid("user@example.com"));
  }

  // -------------------------
  // 휴대폰 번호 테스트
  // -------------------------
  @Test
  void isPhonenumberValid_실패_숫자아님() {
    assertFalse(Validator.isPhonenumberValid("010abc5678")); // 문자 포함
  }

  @Test
  void isPhonenumberValid_실패_시작번호이상함() {
    assertFalse(Validator.isPhonenumberValid("02012345678")); // 01로 시작 안 함
  }

  @Test
  void isPhonenumberValid_실패_자리수초과() {
    assertFalse(Validator.isPhonenumberValid("010123456789")); // 너무 김
  }

  @Test
  void isPhonenumberValid_성공() {
    assertTrue(Validator.isPhonenumberValid("01012345678"));
  }

  // -------------------------
  // 비밀번호 테스트
  // -------------------------
  @Test
  void isPasswordValid_실패_영문없음() {
    assertFalse(Validator.isPasswordValid("12345678!")); // 영문 없음
  }

  @Test
  void isPasswordValid_실패_숫자없음() {
    assertFalse(Validator.isPasswordValid("Password!")); // 숫자 없음
  }

  @Test
  void isPasswordValid_실패_특수문자없음() {
    assertFalse(Validator.isPasswordValid("Password1")); // 특수문자 없음
  }

  @Test
  void isPasswordValid_성공() {
    assertTrue(Validator.isPasswordValid("Password1!"));
  }
}
