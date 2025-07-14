package com.nexus.sion.feature.project.command.application.service;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.notification.command.application.service.NotificationCommandService;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
import com.nexus.sion.feature.project.command.application.dto.request.WorkHistoryAddRequestDto;
import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWork;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkHistoryRepository;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkHistoryTechStackRepository;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DeveloperProjectWorkCommandServiceImplTest {

  @InjectMocks
  private DeveloperProjectWorkCommandServiceImpl service;

  @Mock
  private DeveloperProjectWorkRepository workRepository;

  @Mock
  private DeveloperProjectWorkHistoryRepository workHistoryRepository;

  @Mock
  private DeveloperProjectWorkHistoryTechStackRepository workHistoryTechStackRepository;

  @Mock
  private NotificationCommandService notificationCommandService;

  @Mock
  private MemberRepository memberRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("승인 성공 케이스")
  void approve_success() {
    DeveloperProjectWork work = DeveloperProjectWork.builder()
            .id(1L)
            .employeeIdentificationNumber("EMP001")
            .projectCode("PJT001")
            .build();

    when(memberRepository.existsByEmployeeIdentificationNumberAndRole("ADMIN001", MemberRole.ADMIN)).thenReturn(true);
    when(workRepository.findById(1L)).thenReturn(Optional.of(work));
    when(memberRepository.findEmployeeNameByEmployeeIdentificationNumber("EMP001")).thenReturn(Optional.of("홍길동"));

    service.approve(1L, "ADMIN001");

    verify(notificationCommandService).createAndSendNotification(
            eq("ADMIN001"), eq("EMP001"), anyString(), eq(NotificationType.TASK_APPROVAL_RESULT), eq("1")
    );
  }

  @Test
  @DisplayName("거부 후 재요청 알림 포함")
  void reject_success_and_requestAgain() {
    DeveloperProjectWork work = DeveloperProjectWork.builder()
            .id(1L)
            .employeeIdentificationNumber("EMP001")
            .projectCode("PJT001")
            .build();

    when(memberRepository.existsByEmployeeIdentificationNumberAndRole("ADMIN001", MemberRole.ADMIN)).thenReturn(true);
    when(workRepository.findById(1L)).thenReturn(Optional.of(work));
    when(memberRepository.findEmployeeNameByEmployeeIdentificationNumber("EMP001")).thenReturn(Optional.of("홍길동"));
    when(workRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.reject(1L, "ADMIN001", "기술 부족");

    verify(notificationCommandService, times(2)).createAndSendNotification(any(), any(), any(), any(), any());
    verify(workRepository).save(any());
  }

  @Test
  @DisplayName("비관리자가 승인 시도 -> 예외")
  void approve_fail_ifNotAdmin() {
    when(memberRepository.existsByEmployeeIdentificationNumberAndRole("USER001", MemberRole.ADMIN)).thenReturn(false);

    assertThatThrownBy(() -> service.approve(1L, "USER001"))
            .isInstanceOf(BusinessException.class)
            .hasMessage(ErrorCode.UNAUTHORIZED_APPROVER.getMessage());
  }

  @Test
  @DisplayName("작업 이력 추가 시 알림 전송")
  void addHistories_success() throws Exception {
    DeveloperProjectWork work = DeveloperProjectWork.builder()
            .id(10L)
            .employeeIdentificationNumber("EMP001")
            .projectCode("PJT001")
            .build();

    // 리플렉션으로 WorkHistoryItemDto 값 설정
    WorkHistoryAddRequestDto.WorkHistoryItemDto item = new WorkHistoryAddRequestDto.WorkHistoryItemDto();
    setField(item, "functionName", "업무1");
    setField(item, "functionDescription", "설명");
    setField(item, "functionType", "EI");
    setField(item, "det", "5");
    setField(item, "ftr", "3");
    setField(item, "techStackNames", List.of("Java", "Spring"));

    WorkHistoryAddRequestDto dto = new WorkHistoryAddRequestDto();
    setField(dto, "histories", List.of(item));

    when(workRepository.findById(10L)).thenReturn(Optional.of(work));
    when(memberRepository.findAllByRole(MemberRole.ADMIN))
            .thenReturn(List.of(Member.builder().employeeIdentificationNumber("ADMIN001").build()));

    service.addHistories(10L, dto);

    verify(workHistoryRepository).save(any());
    verify(workHistoryTechStackRepository).saveAll(any());
    verify(notificationCommandService).createAndSendNotification(
            eq("EMP001"), eq("ADMIN001"), isNull(), eq(NotificationType.TASK_APPROVAL_REQUEST), eq("10")
    );
  }

  private static void setField(Object target, String fieldName, Object value) throws Exception {
    var field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }
}
