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
import com.nexus.sion.feature.member.command.application.dto.request.MemberCreateRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    @InjectMocks
    private MemberCommandService userService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    private MemberCreateRequest request;

    @BeforeEach
    void setUp() {
        request = new MemberCreateRequest("EMP123", "홍길동", "Password123!", "01011111111",
                        "test@example.com");
    }

    @Test
    void registerUser_성공() {
        // given
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(memberRepository.existsByEmployeeIdentificationNumber(
                        request.getEmployeeIdentificationNumber())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        Member mappedMember = mock(Member.class);
        when(modelMapper.map(eq(request), eq(Member.class))).thenReturn(mappedMember);
        doNothing().when(mappedMember).setEncodedPassword("encodedPassword");

        // when
        userService.registerUser(request);

        // then
        verify(mappedMember).setEncodedPassword("encodedPassword");
        verify(memberRepository).save(mappedMember);
    }

    @Test
    void registerUser_실패_비밀번호형식불일치() {
        // given
        MemberCreateRequest invalidRequest = new MemberCreateRequest("EMP123", "홍길동", "Password1",
                        "01011111111", "test@example.com");
        // when
        BusinessException exception = assertThrows(BusinessException.class,
                        () -> userService.registerUser(invalidRequest));

        // then
        assertEquals(ErrorCode.INVALID_PASSWORD_FORMAT, exception.getErrorCode());
    }

    @Test
    void registerUser_실패_중복이메일() {
        // given
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                        () -> userService.registerUser(request));

        // then
        assertEquals(ErrorCode.ALREADY_REGISTERED_EMAIL, exception.getErrorCode());
    }

    @Test
    void registerUser_실패_중복사번() {
        // given
        when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(memberRepository.existsByEmployeeIdentificationNumber(
                        request.getEmployeeIdentificationNumber())).thenReturn(true);

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                        () -> userService.registerUser(request));

        // then
        assertEquals(ErrorCode.ALREADY_REGISTERED_EMPLOYEE_IDENTIFICATION_NUMBER,
                        exception.getErrorCode());
    }

    @Test
    void registerUser_실패_휴대폰번호형식불일치() {
        // given
        MemberCreateRequest invalidRequest = new MemberCreateRequest("EMP123", "홍길동",
                        "Password123!", "010-1234-5678", "test@example.com"); // 하이픈 포함된 잘못된 번호

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                        () -> userService.registerUser(invalidRequest));

        // then
        assertEquals(ErrorCode.INVALID_PHONE_NUMBER_FORMAT, exception.getErrorCode());
    }

    @Test
    void registerUser_실패_이메일형식불일치() {
        // given
        MemberCreateRequest invalidRequest = new MemberCreateRequest("EMP123", "홍길동",
                        "Password123!", "01012345678", "invalid-email" // @
                                                                       // 없음
        );

        // when
        BusinessException exception = assertThrows(BusinessException.class,
                        () -> userService.registerUser(invalidRequest));

        // then
        assertEquals(ErrorCode.INVALID_EMAIL_FORMAT, exception.getErrorCode());
    }
}
