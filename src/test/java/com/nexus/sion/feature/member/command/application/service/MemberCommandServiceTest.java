package com.nexus.sion.feature.member.command.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberStatus;
import com.nexus.sion.feature.member.command.domain.repository.*;

@ExtendWith(MockitoExtension.class)
class MemberCommandServiceTest {

  @InjectMocks private MemberCommandService memberCommandService;

  @Mock private MemberRepository memberRepository;
  @Mock private PositionRepository positionRepository;
  @Mock private DeveloperTechStackRepository developerTechStackRepository;
  @Mock private DeveloperTechStackHistoryRepository developerTechStackHistoryRepository;
  @Mock private MemberScoreHistoryRepository memberScoreHistoryRepository;
  @Mock private DepartmentRepository departmentRepository;
  @Mock private InitialScoreRepository initialScoreRepository;
  @Mock private GradeRepository gradeRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private ModelMapper modelMapper;

  private final String EMP_ID = "EMP001";
  private final String EMAIL = "test@example.com";
  private final String PASSWORD = "Password123!";

  private MemberCreateRequest request;

  @BeforeEach
  void setUp() {
    request = new MemberCreateRequest(EMP_ID, "홍길동", PASSWORD, "01011111111", EMAIL, "19800102");
  }

  @Test
  @DisplayName("회원 등록 성공")
  void registerUser_success() {
    // given
    when(memberRepository.existsByEmail(EMAIL)).thenReturn(false);
    when(memberRepository.existsByEmployeeIdentificationNumber(EMP_ID)).thenReturn(false);
    when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

    Member mappedMember = mock(Member.class);
    when(modelMapper.map(eq(request), eq(Member.class))).thenReturn(mappedMember);

    // when
    memberCommandService.registerUser(request);

    // then
    verify(mappedMember).setEncodedPassword("encodedPassword");
    verify(mappedMember).setAdminRole();
    verify(memberRepository).save(mappedMember);
  }

  @Test
  @DisplayName("회원 등록 실패 - 잘못된 비밀번호 형식")
  void registerUser_fail_invalidPassword() {
    // given
    MemberCreateRequest invalidRequest =
        new MemberCreateRequest(EMP_ID, "홍길동", "weakpw", "01011111111", EMAIL, "19800102");

    // when & then
    BusinessException exception =
        assertThrows(
            BusinessException.class, () -> memberCommandService.registerUser(invalidRequest));
    assertEquals(ErrorCode.INVALID_PASSWORD_FORMAT, exception.getErrorCode());
  }

  @Test
  @DisplayName("회원 등록 실패 - 중복 이메일")
  void registerUser_fail_duplicateEmail() {
    when(memberRepository.existsByEmail(EMAIL)).thenReturn(true);

    BusinessException ex =
        assertThrows(BusinessException.class, () -> memberCommandService.registerUser(request));

    assertEquals(ErrorCode.ALREADY_REGISTERED_EMAIL, ex.getErrorCode());
  }

  @Test
  @DisplayName("회원 등록 실패 - 중복 사번")
  void registerUser_fail_duplicateEmployeeId() {
    when(memberRepository.existsByEmail(EMAIL)).thenReturn(false);
    when(memberRepository.existsByEmployeeIdentificationNumber(EMP_ID)).thenReturn(true);

    BusinessException ex =
        assertThrows(BusinessException.class, () -> memberCommandService.registerUser(request));

    assertEquals(ErrorCode.ALREADY_REGISTERED_EMPLOYEE_IDENTIFICATION_NUMBER, ex.getErrorCode());
  }

  @Test
  @DisplayName("회원 등록 실패 - 잘못된 휴대폰 번호")
  void registerUser_fail_invalidPhoneNumber() {
    MemberCreateRequest invalidRequest =
        new MemberCreateRequest(EMP_ID, "홍길동", PASSWORD, "010-1234-5678", EMAIL, "19800102");

    BusinessException ex =
        assertThrows(
            BusinessException.class, () -> memberCommandService.registerUser(invalidRequest));

    assertEquals(ErrorCode.INVALID_PHONE_NUMBER_FORMAT, ex.getErrorCode());
  }

  @Test
  @DisplayName("회원 등록 실패 - 잘못된 이메일 형식")
  void registerUser_fail_invalidEmailFormat() {
    MemberCreateRequest invalidRequest =
        new MemberCreateRequest(
            EMP_ID, "홍길동", PASSWORD, "01012345678", "invalid-email", "19800102");

    BusinessException ex =
        assertThrows(
            BusinessException.class, () -> memberCommandService.registerUser(invalidRequest));

    assertEquals(ErrorCode.INVALID_EMAIL_FORMAT, ex.getErrorCode());
  }

  @Test
  @DisplayName("구성원 일괄 등록 성공")
  void addMembers_success() {
    MemberAddRequest req =
        new MemberAddRequest(
            EMP_ID,
            "홍길동",
            "01012345678",
            LocalDate.of(1995, 1, 1),
            LocalDate.of(2020, 1, 1),
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
    when(initialScoreRepository.findByCareerYears(3)).thenReturn(Optional.of(mockInitialScore));
    when(passwordEncoder.encode(anyString())).thenReturn("encoded");
    when(gradeRepository.findAllByOrderByScoreThresholdDesc()).thenReturn(List.of());

    // when
    memberCommandService.addMembers(List.of(req));

    // then
    verify(memberRepository).save(any(Member.class));
    verify(developerTechStackRepository, atLeastOnce()).save(any());
    verify(developerTechStackHistoryRepository, atLeastOnce()).save(any());
    verify(memberScoreHistoryRepository, atLeastOnce()).save(any()); // ✅ 검증도 추가 가능
  }

  @Test
  @DisplayName("구성원 수정 성공")
  void updateMember_success() {
    MemberUpdateRequest req =
        new MemberUpdateRequest(
            "홍길동",
            "01012345678",
            LocalDate.of(1990, 1, 1),
            LocalDate.of(2022, 1, 1),
            "hong@example.com",
            4,
            "백엔드",
            "개발팀",
            null,
            6000L,
            List.of("JAVA", "SPRING"));

    Member member =
        Member.builder().employeeIdentificationNumber(EMP_ID).email("hong@example.com").build();

    when(memberRepository.findById(EMP_ID)).thenReturn(Optional.of(member));
    when(positionRepository.existsById("백엔드")).thenReturn(true);
    when(departmentRepository.existsById("개발팀")).thenReturn(true);
    when(initialScoreRepository.findByCareerYears(4)).thenReturn(Optional.empty());
    when(developerTechStackRepository.findAllByEmployeeIdentificationNumber(EMP_ID))
        .thenReturn(List.of());

    memberCommandService.updateMember(EMP_ID, req);

    verify(developerTechStackRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("구성원 수정 실패 - 존재하지 않는 멤버")
  void updateMember_fail_notFound() {
    when(memberRepository.findById(EMP_ID)).thenReturn(Optional.empty());
    MemberUpdateRequest req = mock(MemberUpdateRequest.class);

    assertThrows(BusinessException.class, () -> memberCommandService.updateMember(EMP_ID, req));
  }

  @Test
  @DisplayName("구성원 삭제 실패 - 존재하지 않는 멤버")
  void deleteMember_fail_notFound() {
    when(memberRepository.findById(EMP_ID)).thenReturn(Optional.empty());

    assertThrows(BusinessException.class, () -> memberCommandService.deleteMember(EMP_ID));
  }

  @Test
  @DisplayName("구성원 삭제 실패 - 이미 삭제된 멤버")
  void deleteMember_fail_alreadyDeleted() {
    Member deletedMember =
        Member.builder()
            .employeeIdentificationNumber(EMP_ID)
            .deletedAt(LocalDateTime.now())
            .role(MemberRole.INSIDER)
            .build();

    when(memberRepository.findById(EMP_ID)).thenReturn(Optional.of(deletedMember));

    BusinessException ex =
        assertThrows(BusinessException.class, () -> memberCommandService.deleteMember(EMP_ID));

    assertEquals(ErrorCode.ALREADY_DELETED_USER, ex.getErrorCode());
  }

  @Test
  @DisplayName("구성원 삭제 실패 - 관리자 계정")
  void deleteMember_fail_admin() {
    Member admin =
        Member.builder().employeeIdentificationNumber(EMP_ID).role(MemberRole.ADMIN).build();

    when(memberRepository.findById(EMP_ID)).thenReturn(Optional.of(admin));

    BusinessException ex =
        assertThrows(BusinessException.class, () -> memberCommandService.deleteMember(EMP_ID));

    assertEquals(ErrorCode.CANNOT_DELETE_ADMIN, ex.getErrorCode());
  }

  @Test
  @DisplayName("구성원 삭제 성공")
  void deleteMember_success() {
    Member member =
        Member.builder().employeeIdentificationNumber(EMP_ID).role(MemberRole.INSIDER).build();

    when(memberRepository.findById(EMP_ID)).thenReturn(Optional.of(member));

    memberCommandService.deleteMember(EMP_ID);

    assertNotNull(member.getDeletedAt());
  }

  @Test
  @DisplayName("구성원 상태 변경 성공")
  void updateMemberStatus_success() {
    Member member = mock(Member.class);
    when(memberRepository.findById(EMP_ID)).thenReturn(Optional.of(member));

    memberCommandService.updateMemberStatus(EMP_ID, MemberStatus.UNAVAILABLE);

    verify(member).updateStatus(MemberStatus.UNAVAILABLE);
  }
}
