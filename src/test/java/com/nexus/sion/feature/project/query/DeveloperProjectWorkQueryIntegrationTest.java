package com.nexus.sion.feature.project.query;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;
import com.nexus.sion.feature.project.command.domain.aggregate.*;
import com.nexus.sion.feature.project.command.repository.ClientCompanyRepository;
import com.nexus.sion.feature.project.command.repository.DeveloperProjectWorkRepository;
import com.nexus.sion.feature.project.command.domain.repository.ProjectRepository;
import com.nexus.sion.feature.project.command.repository.DomainRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class DeveloperProjectWorkQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private DeveloperProjectWorkRepository workRepository;
  @Autowired private MemberRepository memberRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private ClientCompanyRepository clientCompanyRepository;
  @Autowired private DomainRepository domainRepository;

  private static final String TEST_ADMIN_ID = "ADMIN001";

  private Long testWorkId;

  @BeforeEach
  void setUp() {
    testWorkId = createWorkAndAdmin();
  }

  private void createTestDomainIfNotExists() {
    if (!domainRepository.existsById("test-domain")) {
      domainRepository.save(Domain.of("test-domain"));
    }
  }

  private void createTestClientCompanyIfNotExists() {
    createTestDomainIfNotExists();

    if (!clientCompanyRepository.existsById("CLIENT001")) {
      clientCompanyRepository.save(ClientCompany.builder()
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

    Member admin = memberRepository.save(
            Member.builder()
                    .employeeIdentificationNumber(TEST_ADMIN_ID)
                    .employeeName("홍길동")
                    .password("encodedPassword")
                    .phoneNumber("01012345678")
                    .email("admin@nexus.com")
                    .role(MemberRole.ADMIN)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build()
    );

    Project project = projectRepository.save(
            Project.builder()
                    .projectCode("PROJ001")
                    .title("테스트 프로젝트")
                    .domainName("test-domain")
                    .description("설명")
                    .budget(10_000_000L)
                    .startDate(LocalDate.now())
                    .expectedEndDate(LocalDate.now().plusMonths(3))
                    .status(Project.ProjectStatus.IN_PROGRESS)
                    .clientCode("CLIENT001")
                    .requestSpecificationUrl("https://example.com/spec")
                    .build()
    );

    DeveloperProjectWork work = workRepository.save(
            DeveloperProjectWork.builder()
                    .employeeIdentificationNumber(admin.getEmployeeIdentificationNumber())
                    .projectCode(project.getProjectCode())
                    .approvalStatus(DeveloperProjectWork.ApprovalStatus.PENDING)
                    .build()
    );

    return work.getId();
  }

  @Test
  @DisplayName("관리자 작업 요청 목록 조회 성공 - 필드 검증 포함")
  void getRequestsForAdmin() throws Exception {
    mockMvc.perform(get("/api/v1/dev-project-works/admin")
                    .param("status", "PENDING")
                    .param("page", "0")
                    .param("size", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content[0].projectCode").value("PROJ001"))
            .andExpect(jsonPath("$.data.content[0].employeeId").value(TEST_ADMIN_ID))
            .andExpect(jsonPath("$.data.content[0].approvalStatus").value("PENDING"))
            .andExpect(jsonPath("$.data.totalElements").value(1));
  }

  @Test
  @DisplayName("기능 유형 목록 조회 성공 - Enum 전체 검증")
  void getFunctionTypes() throws Exception {
    mockMvc.perform(get("/api/v1/dev-project-works/function-types"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(DeveloperProjectWorkHistory.FunctionType.values().length))
            .andExpect(jsonPath("$.data[0].code").isNotEmpty())
            .andExpect(jsonPath("$.data[0].name").isNotEmpty());
  }

  @Test
  @DisplayName("작업 이력 상세 조회 성공 - 필드 검증 포함")
  void getProjectHistoryDetail() throws Exception {
    mockMvc.perform(get("/api/v1/dev-project-works/{projectWorkId}", testWorkId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.projectCode").value("PROJ001"))
            .andExpect(jsonPath("$.data.approvalStatus").value("PENDING"))
            .andExpect(jsonPath("$.data.projectTitle").value("테스트 프로젝트"));
  }
}
