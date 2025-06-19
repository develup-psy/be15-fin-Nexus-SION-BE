package com.nexus.sion.feature.member.command.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.UserCreateRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @InjectMocks private UserCommandService userService;

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private ModelMapper modelMapper;

  private UserCreateRequest request;

  @BeforeEach
  void setUp() {
    request =
        new UserCreateRequest(
            "EMP123", "홍길동", "test@example.com", "Password123!" // 유효한 비밀번호라고 가정
            );
  }

  @Test
  void registerUser_성공() {
    // given
    when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(userRepository.existsByEmployeeIdentificationNumber(
            request.getEmployeeIdentificationNumber()))
        .thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

    Member mappedMember = mock(Member.class);
    when(modelMapper.map(eq(request), eq(Member.class))).thenReturn(mappedMember);
    doNothing().when(mappedMember).setEncodedPassword("encodedPassword");

    // when
    userService.registerUser(request);

    // then
    verify(mappedMember).setEncodedPassword("encodedPassword");
    verify(userRepository).save(mappedMember);
  }

  @Test
  void registerUser_실패_비밀번호형식불일치() {
    // given
    UserCreateRequest invalidRequest =
        new UserCreateRequest("EMP123", "홍길동", "test@example.com", "weak");

    // when
    BusinessException exception =
        assertThrows(BusinessException.class, () -> userService.registerUser(invalidRequest));

    // then
    assertEquals(ErrorCode.INVALID_PASSWORD_FORMAT, exception.getErrorCode());
  }

  @Test
  void registerUser_실패_중복이메일() {
    // given
    when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

    // when
    BusinessException exception =
        assertThrows(BusinessException.class, () -> userService.registerUser(request));

    // then
    assertEquals(ErrorCode.ALREADY_REGISTERED_EMAIL, exception.getErrorCode());
  }

  @Test
  void registerUser_실패_중복사번() {
    // given
    when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(userRepository.existsByEmployeeIdentificationNumber(
            request.getEmployeeIdentificationNumber()))
        .thenReturn(true);

    // when
    BusinessException exception =
        assertThrows(BusinessException.class, () -> userService.registerUser(request));

    // then
    assertEquals(
        ErrorCode.ALREADY_REGISTERED_EMPLOYEE_IDENTIFICATION_NUMBER, exception.getErrorCode());
  }
}
