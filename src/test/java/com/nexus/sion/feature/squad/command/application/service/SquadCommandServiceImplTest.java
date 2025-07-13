package com.nexus.sion.feature.squad.command.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.squad.command.application.dto.request.Developer;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadUpdateRequest;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.service.*;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
import com.nexus.sion.feature.squad.command.repository.SquadCommentRepository;
import com.nexus.sion.feature.squad.command.repository.SquadEmployeeCommandRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class SquadCommandServiceImplTest {

  @InjectMocks private SquadCommandServiceImpl squadCommandService;

  @Mock private SquadCommandRepository squadCommandRepository;
  @Mock private SquadEmployeeCommandRepository squadEmployeeCommandRepository;
  @Mock private SquadCommentRepository squadCommentRepository;
  @Mock private SquadValidationService squadValidationService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("스쿼드 수동 등록 성공")
  void registerManualSquad_success() {
    // given
    Developer developer = Developer.builder().employeeId("EMP001").projectAndJobId(101L).build();

    SquadRegisterRequest request = SquadRegisterRequest.builder()
            .projectCode("ha_1_1")
            .title("프론트엔드팀")
            .description("설명입니다.")
            .developers(List.of(developer))
            .estimatedCost(BigDecimal.valueOf(5000000))

            .estimatedDuration(BigDecimal.valueOf(3))
            .build();

    Project project = Project.builder().projectCode("ha_1_1").clientCode("ha_1").build();

    given(squadValidationService.validateAndGetProject("ha_1_1")).willReturn(project);
    willDoNothing().given(squadValidationService).validateSquadTitleUniqueForCreate("프론트엔드팀", "ha_1_1");
    willDoNothing().given(squadValidationService).validateDevelopersExist(List.of(developer));
    willDoNothing().given(squadValidationService).validateJobRequirements("ha_1_1", List.of(developer));
    willDoNothing().given(squadValidationService).validateBudget(project, BigDecimal.valueOf(5000000));
    willDoNothing().given(squadValidationService).validateDuration(project, BigDecimal.valueOf(3));
    given(squadCommandRepository.countByProjectCode("ha_1_1")).willReturn(0L);

    // when
    squadCommandService.registerManualSquad(request);

    // then
    then(squadCommandRepository).should(times(1)).save(any(Squad.class));
    then(squadEmployeeCommandRepository).should(times(1)).saveAll(anyList());
  }

  @Test
  @DisplayName("스쿼드 등록 실패 - 프로젝트가 존재하지 않음")
  void registerManualSquad_projectNotFound() {
    // given
    SquadRegisterRequest request = SquadRegisterRequest.builder()
            .projectCode("invalid_code")
            .title("스쿼드명")
            .description("설명")
            .developers(List.of())
            .build();

    given(squadValidationService.validateAndGetProject("invalid_code"))
            .willThrow(new BusinessException(ErrorCode.PROJECT_NOT_FOUND));

    // when & then
    assertThatThrownBy(() -> squadCommandService.registerManualSquad(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage(ErrorCode.PROJECT_NOT_FOUND.getMessage());

    then(squadCommandRepository).shouldHaveNoInteractions();
    then(squadEmployeeCommandRepository).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("스쿼드 수정 성공")
  void updateManualSquad_success() {
    // given
    String squadCode = "ha_1_1_1";
    Developer dev1 = new Developer("EMP001", 101L, false);
    Developer dev2 = new Developer("EMP002", 102L, false);

    SquadUpdateRequest request = SquadUpdateRequest.builder()
            .squadCode(squadCode)
            .title("수정된 제목")
            .description("수정 설명")
            .developers(List.of(dev1, dev2))
            .estimatedCost(BigDecimal.valueOf(4000000))
            .estimatedDuration(BigDecimal.valueOf(2))
            .build();

    Squad squad = Squad.builder()
            .squadCode(squadCode)
            .projectCode("ha_1_1")
            .title("기존 제목")
            .description("기존 설명")
            .build();

    Project project = Project.builder().projectCode("ha_1_1").build();

    given(squadCommandRepository.findBySquadCode(squadCode)).willReturn(Optional.of(squad));
    given(squadValidationService.validateAndGetProject("ha_1_1")).willReturn(project);

    willDoNothing().given(squadValidationService)
            .validateSquadTitleUniqueForUpdate("수정된 제목", "ha_1_1", squadCode);
    willDoNothing().given(squadValidationService).validateDevelopersExist(any());
    willDoNothing().given(squadValidationService).validateJobRequirements(any(), any());
    willDoNothing().given(squadValidationService).validateBudget(project, BigDecimal.valueOf(4000000));
    willDoNothing().given(squadValidationService).validateDuration(project, BigDecimal.valueOf(2));

    // when
    squadCommandService.updateManualSquad(request);

    // then
    assertThat(squad.getTitle()).isEqualTo("수정된 제목");
    assertThat(squad.getDescription()).isEqualTo("수정 설명");

    then(squadEmployeeCommandRepository).should(times(1)).deleteBySquadCode(squadCode);
    then(squadEmployeeCommandRepository).should(times(1)).saveAll(anyList());
  }

  @Test
  @DisplayName("스쿼드 수정 실패 - 존재하지 않는 스쿼드")
  void updateManualSquad_notFound() {
    // given
    SquadUpdateRequest request = SquadUpdateRequest.builder()
            .squadCode("invalid_code")
            .title("제목")
            .description("설명")
            .developers(List.of())
            .build();

    given(squadCommandRepository.findBySquadCode("invalid_code")).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> squadCommandService.updateManualSquad(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("스쿼드");

    then(squadEmployeeCommandRepository).shouldHaveNoInteractions();
  }

  @Test
  @DisplayName("스쿼드 삭제 성공")
  void deleteSquad_success() {
    // given
    String squadCode = "ha_1_1_1";
    Squad squad = Squad.builder()
            .squadCode(squadCode)
            .projectCode("ha_1_1")
            .title("삭제할 스쿼드")
            .build();

    given(squadCommandRepository.findBySquadCode(squadCode)).willReturn(Optional.of(squad));

    // when
    squadCommandService.deleteSquad(squadCode);

    // then
    then(squadEmployeeCommandRepository).should(times(1)).deleteBySquadCode(squadCode);
    then(squadCommentRepository).should(times(1)).deleteBySquadCode(squadCode);
    then(squadCommandRepository).should(times(1)).delete(squad);
  }

  @Test
  @DisplayName("스쿼드 삭제 실패 - 존재하지 않음")
  void deleteSquad_notFound() {
    // given
    String squadCode = "INVALID_CODE";
    given(squadCommandRepository.findBySquadCode(squadCode)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> squadCommandService.deleteSquad(squadCode))
            .isInstanceOf(BusinessException.class)
            .hasMessage(ErrorCode.SQUAD_NOT_FOUND.getMessage());

    then(squadEmployeeCommandRepository).shouldHaveNoInteractions();
    then(squadCommentRepository).shouldHaveNoInteractions();
    then(squadCommandRepository).should(never()).delete(any());
  }
}
