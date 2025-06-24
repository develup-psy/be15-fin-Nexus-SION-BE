package com.nexus.sion.feature.member.command.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
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
import com.nexus.sion.feature.member.command.application.dto.request.MemberAddRequest;
import com.nexus.sion.feature.member.command.application.dto.request.MemberCreateRequest;
import com.nexus.sion.feature.member.command.application.dto.request.MemberUpdateRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.InitialScore;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.repository.DepartmentRepository;
import com.nexus.sion.feature.member.command.domain.repository.DeveloperTechStackRepository;
import com.nexus.sion.feature.member.command.domain.repository.InitialScoreRepository;
import com.nexus.sion.feature.member.command.domain.repository.PositionRepository;
import com.nexus.sion.feature.member.command.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

  @InjectMocks private MemberCommandService userService;

  @Mock private MemberRepository memberRepository;
  @Mock private PositionRepository positionRepository;
  @Mock private DeveloperTechStackRepository developerTechStackRepository;
  @Mock private DepartmentRepository departmentRepository;
  @Mock private InitialScoreRepository initialScoreRepository;
  @InjectMocks private MemberCommandService memberCommandService;

  @Mock private PasswordEncoder passwordEncoder;

  @Mock private ModelMapper modelMapper;

  private MemberCreateRequest request;

  private final String EMP_ID = "EMP001";

  @BeforeEach
  void setUp() {
    request =
        new MemberCreateRequest("EMP123", "홍길동", "Password123!", "01011111111", "test@example.com");
  }

  @Test
  void registerUser_성공() {
    // given
    when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(memberRepository.existsByEmployeeIdentificationNumber(
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
    verify(memberRepository).save(mappedMember);
  }

  @Test
  void registerUser_실패_비밀번호형식불일치() {
    // given
    MemberCreateRequest invalidRequest =
        new MemberCreateRequest("EMP123", "홍길동", "Password1", "01011111111", "test@example.com");
    // when
    BusinessException exception =
        assertThrows(BusinessException.class, () -> userService.registerUser(invalidRequest));

    // then
    assertEquals(ErrorCode.INVALID_PASSWORD_FORMAT, exception.getErrorCode());
  }

  @Test
  void registerUser_실패_중복이메일() {
    // given
    when(memberRepository.existsByEmail(request.getEmail())).thenReturn(true);

    // when
    BusinessException exception =
        assertThrows(BusinessException.class, () -> userService.registerUser(request));

    // then
    assertEquals(ErrorCode.ALREADY_REGISTERED_EMAIL, exception.getErrorCode());
  }

  @Test
  void registerUser_실패_중복사번() {
    // given
    when(memberRepository.existsByEmail(request.getEmail())).thenReturn(false);
    when(memberRepository.existsByEmployeeIdentificationNumber(
            request.getEmployeeIdentificationNumber()))
        .thenReturn(true);

    // when
    BusinessException exception =
        assertThrows(BusinessException.class, () -> userService.registerUser(request));

    // then
    assertEquals(
        ErrorCode.ALREADY_REGISTERED_EMPLOYEE_IDENTIFICATION_NUMBER, exception.getErrorCode());
  }

  @Test
  void registerUser_실패_휴대폰번호형식불일치() {
    // given
    MemberCreateRequest invalidRequest =
        new MemberCreateRequest(
            "EMP123", "홍길동", "Password123!", "010-1234-5678", "test@example.com"); // 하이픈 포함된 잘못된 번호

    // when
    BusinessException exception =
        assertThrows(BusinessException.class, () -> userService.registerUser(invalidRequest));

    // then
    assertEquals(ErrorCode.INVALID_PHONE_NUMBER_FORMAT, exception.getErrorCode());
  }

  @Test
  void registerUser_실패_이메일형식불일치() {
    // given
    MemberCreateRequest invalidRequest =
        new MemberCreateRequest(
            "EMP123", "홍길동", "Password123!", "01012345678", "invalid-email" // @
            // 없음
            );

    // when
    BusinessException exception =
        assertThrows(BusinessException.class, () -> userService.registerUser(invalidRequest));

    // then
    assertEquals(ErrorCode.INVALID_EMAIL_FORMAT, exception.getErrorCode());
  }

  @Test
  void addMembers_success() {
    // given
    MemberAddRequest req =
        new MemberAddRequest(
            EMP_ID,
            "홍길동",
            "01012345678",
            LocalDate.of(1995, 1, 1),
            LocalDateTime.of(2020, 1, 1, 0, 9),
            "hong@example.com",
            3,
            "백엔드",
            "개발팀",
            null,
            5000L,
            List.of("JAVA"));

    InitialScore mockInitialScore = mock(InitialScore.class);
    when(positionRepository.existsById("백엔드")).thenReturn(true);
    when(departmentRepository.existsById("개발팀")).thenReturn(true);
    when(memberRepository.existsByEmail(req.email())).thenReturn(false);
    when(memberRepository.existsByEmployeeIdentificationNumber(EMP_ID)).thenReturn(false);
    when(initialScoreRepository.findTopByYearsLessThanEqualOrderByYearsDesc(3))
        .thenReturn(Optional.of(mockInitialScore));
    when(passwordEncoder.encode(anyString())).thenReturn("encoded");

    // when
    memberCommandService.addMembers(List.of(req));

    // then
    verify(memberRepository).save(any(Member.class));
    verify(developerTechStackRepository).saveAll(anyList());
  }

  @Test
  void updateMember_success() {
    // given
    MemberUpdateRequest req =
        new MemberUpdateRequest(
            "홍길동",
            "01012345678",
            LocalDate.of(1990, 1, 1),
            LocalDateTime.of(2022, 1, 1, 0, 0),
            "hong@example.com",
            4,
            "백엔드",
            "개발팀",
            null,
            6000L,
            List.of("JAVA", "SPRING"));

    Member member =
        Member.builder().employeeIdentificationNumber(EMP_ID).email("hong@example.com").build();

    InitialScore mockInitialScore = mock(InitialScore.class);
    when(memberRepository.findById(EMP_ID)).thenReturn(Optional.of(member));
    when(positionRepository.existsById("백엔드")).thenReturn(true);
    when(departmentRepository.existsById("개발팀")).thenReturn(true);
    when(initialScoreRepository.findTopByYearsLessThanEqualOrderByYearsDesc(anyInt()))
        .thenReturn(Optional.of(mockInitialScore));
    when(developerTechStackRepository.findAllByEmployeeIdentificationNumber(EMP_ID))
        .thenReturn(List.of());

    // when
    memberCommandService.updateMember(EMP_ID, req);

    // then
    verify(developerTechStackRepository).saveAll(anyList());
  }

  @Test
  void updateMember_throw_if_not_found() {
    when(memberRepository.findById(EMP_ID)).thenReturn(Optional.empty());

    MemberUpdateRequest req = mock(MemberUpdateRequest.class);

    assertThrows(BusinessException.class, () -> memberCommandService.updateMember(EMP_ID, req));
  }


  @Test
  void deleteMember_throws_if_user_not_found() {
    // given
    String empId = "EMP001";
    when(memberRepository.findById(empId)).thenReturn(Optional.empty());

    // when & then
    assertThrows(BusinessException.class, () -> {
      memberCommandService.deleteMember(empId);
    });
  }

  @Test
  void deleteMember_throws_if_already_deleted() {
    // given
    String empId = "EMP002";
    Member deletedMember = Member.builder()
            .employeeIdentificationNumber(empId)
            .deletedAt(LocalDateTime.now())
            .role(MemberRole.INSIDER)
            .build();

    when(memberRepository.findById(empId)).thenReturn(Optional.of(deletedMember));

    // when & then
    BusinessException ex = assertThrows(BusinessException.class, () -> {
      memberCommandService.deleteMember(empId);
    });
    assertEquals(ErrorCode.ALREADY_DELETED_USER, ex.getErrorCode());
  }

  @Test
  void deleteMember_throws_if_admin() {
    // given
    String empId = "ADMIN001";
    Member admin = Member.builder()
            .employeeIdentificationNumber(empId)
            .role(MemberRole.ADMIN)
            .deletedAt(null)
            .build();

    when(memberRepository.findById(empId)).thenReturn(Optional.of(admin));

    // when & then
    BusinessException ex = assertThrows(BusinessException.class, () -> {
      memberCommandService.deleteMember(empId);
    });
    assertEquals(ErrorCode.CANNOT_DELETE_ADMIN, ex.getErrorCode());
  }

  @Test
  void deleteMember_success() {
    // given
    String empId = "EMP003";
    Member member = Member.builder()
            .employeeIdentificationNumber(empId)
            .role(MemberRole.INSIDER)
            .deletedAt(null)
            .build();

    when(memberRepository.findById(empId)).thenReturn(Optional.of(member));

    // when
    memberCommandService.deleteMember(empId);

    // then
    assertNotNull(member.getDeletedAt());
  }
}
