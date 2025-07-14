package com.nexus.sion.feature.project.command.application;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.project.command.application.dto.request.WorkHistoryAddRequestDto;
import com.nexus.sion.feature.project.command.application.dto.request.WorkHistoryAddRequestDto.WorkHistoryItemDto;
import com.nexus.sion.feature.project.command.domain.aggregate.ClientCompany;
import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWork;
import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWork.ApprovalStatus;
import com.nexus.sion.feature.project.command.domain.aggregate.Domain;
import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.project.command.repository.ClientCompanyRepository;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkRepository;
import com.nexus.sion.feature.project.command.repository.DomainRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class DeveloperProjectWorkCommandIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private DeveloperProjectWorkRepository workRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private ClientCompanyRepository clientCompanyRepository;
  @Autowired private DomainRepository domainRepository;

  private static final String TEST_ADMIN_ID = "ADMIN001";

  private void createTestDomainIfNotExists() {
    if (!domainRepository.existsById("test-domain")) {
      domainRepository.save(Domain.of("test-domain"));
    }
  }

  private void createTestClientCompanyIfNotExists() {
    createTestDomainIfNotExists();

    if (!clientCompanyRepository.existsById("CLIENT001")) {
      clientCompanyRepository.save(
          ClientCompany.builder()
              .clientCode("CLIENT001")
              .companyName("테스트 고객사")
              .domainName("test-domain")
              .contactPerson("김담당")
              .email("client@example.com")
              .contactNumber("010-0000-0000")
              .build());
    }
  }

  private Long createWorkAndAdmin() {
    createTestClientCompanyIfNotExists();
    // 관리자 저장
    Member admin =
        memberRepository.save(
            Member.builder()
                .employeeIdentificationNumber("ADMIN001")
                .employeeName("홍길동")
                .password("encodedPassword")
                .phoneNumber("01012345678")
                .email("admin@nexus.com")
                .role(MemberRole.ADMIN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

    // 프로젝트 저장
    Project project =
        projectRepository.save(
            Project.builder()
                .projectCode("PROJ001")
                .title("통합 테스트 프로젝트")
                .domainName("test-domain")
                .description("설명입니다")
                .budget(10_000_000L)
                .startDate(LocalDate.now())
                .expectedEndDate(LocalDate.now().plusMonths(3))
                .status(Project.ProjectStatus.IN_PROGRESS)
                .clientCode("CLIENT001")
                .requestSpecificationUrl("https://example.com/spec")
                .build());

    // 작업 저장
    DeveloperProjectWork work =
        workRepository.save(
            DeveloperProjectWork.builder()
                .employeeIdentificationNumber(admin.getEmployeeIdentificationNumber())
                .projectCode(project.getProjectCode())
                .approvalStatus(ApprovalStatus.PENDING)
                .build());

    return work.getId();
  }

  @Test
  @DisplayName("작업 이력 추가 성공")
  void addHistories_success() throws Exception {
    Long workId = createWorkAndAdmin();

    WorkHistoryItemDto item =
        new WorkHistoryItemDto("기능명", "기능 설명입니다.", "EI", "5", "2", List.of("Java", "Spring Boot"));

    WorkHistoryAddRequestDto dto = new WorkHistoryAddRequestDto(null, List.of(item));

    mockMvc
        .perform(
            put("/api/v1/dev-project-works/{workId}/histories", workId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("작업 이력 승인 성공")
  void approve_success() throws Exception {
    Long workId = createWorkAndAdmin();

    mockMvc
        .perform(
            put("/api/v1/dev-project-works/{id}/approve", workId).param("adminId", TEST_ADMIN_ID))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }

  @Test
  @DisplayName("작업 이력 반려 성공")
  void reject_success() throws Exception {
    Long workId = createWorkAndAdmin();

    mockMvc
        .perform(
            put("/api/v1/dev-project-works/{id}/reject", workId)
                .param("adminId", TEST_ADMIN_ID)
                .param("reason", "내용 미흡"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }
}
