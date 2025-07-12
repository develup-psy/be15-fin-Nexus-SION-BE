package com.nexus.sion.feature.squad.command.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.nexus.sion.feature.squad.command.application.dto.request.Developer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadUpdateRequest;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
import com.nexus.sion.feature.squad.command.repository.SquadCommentRepository;
import com.nexus.sion.feature.squad.command.repository.SquadEmployeeCommandRepository;

class SquadCommandServiceImplTest {

  @InjectMocks private SquadCommandServiceImpl squadCommandService;
  @Mock private SquadCommandRepository squadCommandRepository;
  @Mock private SquadEmployeeCommandRepository squadEmployeeCommandRepository;
  @Mock private ProjectRepository projectRepository;
  @Mock private SquadCommentRepository squadCommentRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("스쿼드 수동 등록 성공")
  void registerManualSquad_success() {
    // given
    Developer developer =
        Developer.builder()
            .employeeId("EMP001")
            .projectAndJobId(101L)
            .build();

    SquadRegisterRequest request =
        SquadRegisterRequest.builder()
            .projectCode("ha_1_1")
            .title("프론트엔드팀")
            .description("설명입니다.")
            .developers(List.of(developer))
            .build();

    Project project = Project.builder().projectCode("ha_1_1").clientCode("ha_1").build();

    when(projectRepository.findByProjectCode("ha_1_1")).thenReturn(Optional.of(project));

    when(squadCommandRepository.countByProjectCode("ha_1_1")).thenReturn(0L);

    // when
    squadCommandService.registerManualSquad(request);

    // then
    verify(projectRepository).findByProjectCode("ha_1_1");
    verify(squadCommandRepository).save(any(Squad.class));
    ArgumentCaptor<List<SquadEmployee>> listCaptor = ArgumentCaptor.forClass(List.class);
    verify(squadEmployeeCommandRepository).saveAll(listCaptor.capture());

    List<SquadEmployee> capturedList = listCaptor.getValue();
    assertThat(capturedList).hasSize(1);
    assertThat(capturedList.get(0).getEmployeeIdentificationNumber()).isEqualTo("EMP001");
  }

  @Test
  @DisplayName("스쿼드 등록 실패 - 프로젝트가 존재하지 않을 경우")
  void registerManualSquad_projectNotFound() {
    // given
    SquadRegisterRequest request =
        SquadRegisterRequest.builder()
            .projectCode("invalid_code")
            .title("스쿼드명")
            .description("설명")
            .developers(List.of())
            .build();

    when(projectRepository.findByProjectCode("invalid_code")).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> squadCommandService.registerManualSquad(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.PROJECT_NOT_FOUND.getMessage());

    verify(projectRepository).findByProjectCode("invalid_code");
    verifyNoInteractions(squadCommandRepository);
    verifyNoInteractions(squadEmployeeCommandRepository);
  }

  @Test
  @DisplayName("스쿼드 수정에 성공한다")
  void updateManualSquad_success() {
    // given
    String squadCode = "ha_1_1_1";
    SquadUpdateRequest request =
        SquadUpdateRequest.builder()
            .squadCode(squadCode)
            .title("수정된 스쿼드 제목")
            .description("수정된 설명")
            .developers(
                List.of(
                    new Developer("EMP001", 101L, false),
                    new Developer("EMP002", 102L, false)))
            .build();

    Squad squad =
        Squad.builder()
            .squadCode(squadCode)
            .projectCode("ha_1_1")
            .title("기존 제목")
            .description("기존 설명")
            .build();

    given(squadCommandRepository.findBySquadCode(squadCode)).willReturn(Optional.of(squad));

    // when
    squadCommandService.updateManualSquad(request);

    // then
    assertThat(squad.getTitle()).isEqualTo("수정된 스쿼드 제목");
    assertThat(squad.getDescription()).isEqualTo("수정된 설명");

    then(squadEmployeeCommandRepository).should(times(1)).deleteBySquadCode(squadCode);

    then(squadEmployeeCommandRepository).should(times(1)).saveAll(anyList());
  }

  @Test
  @DisplayName("존재하지 않는 스쿼드 코드로 수정 시 예외 발생")
  void updateManualSquad_squadNotFound() {
    // given
    String squadCode = "invalid_code";

    SquadUpdateRequest request =
        SquadUpdateRequest.builder()
            .squadCode(squadCode)
            .title("제목")
            .description("설명")
            .developers(List.of())
            .build();

    given(squadCommandRepository.findBySquadCode(squadCode)).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> squadCommandService.updateManualSquad(request))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("스쿼드");
  }

  @Test
  @DisplayName("스쿼드 삭제에 성공한다")
  void deleteSquad_success() {
    // given
    String squadCode = "ha_1_1_1";
    Squad squad =
        Squad.builder()
            .squadCode(squadCode)
            .projectCode("ha_1_1")
            .title("삭제할 스쿼드")
            .description("테스트용")
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
  @DisplayName("스쿼드 삭제 실패 - 존재하지 않는 스쿼드 코드")
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
