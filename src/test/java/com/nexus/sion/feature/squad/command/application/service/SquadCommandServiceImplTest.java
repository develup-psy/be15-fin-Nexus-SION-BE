package com.nexus.sion.feature.squad.command.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;
import com.nexus.sion.feature.squad.command.repository.SquadCommandRepository;
import com.nexus.sion.feature.squad.command.repository.SquadEmployeeCommandRepository;

class SquadCommandServiceImplTest {

  @InjectMocks private SquadCommandServiceImpl squadCommandService;

  @Mock private SquadCommandRepository squadCommandRepository;

  @Mock private SquadEmployeeCommandRepository squadEmployeeCommandRepository;

  @Mock private ProjectRepository projectRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("스쿼드 수동 등록 성공")
  void registerManualSquad_success() {
    // given
    SquadRegisterRequest.Member member1 =
        SquadRegisterRequest.Member.builder()
            .employeeIdentificationNumber("EMP001")
            .projectAndJobId(101L)
            .build();

    SquadRegisterRequest request =
        SquadRegisterRequest.builder()
            .projectCode("ha_1_1")
            .title("프론트엔드팀")
            .description("설명입니다.")
            .members(List.of(member1))
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
            .members(List.of())
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
}
