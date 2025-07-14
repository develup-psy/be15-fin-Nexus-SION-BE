package com.nexus.sion.feature.squad.query;

import static com.example.jooq.generated.Tables.*;
import static com.example.jooq.generated.tables.Squad.SQUAD;
import static com.example.jooq.generated.tables.SquadEmployee.SQUAD_EMPLOYEE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.jooq.generated.enums.MemberGradeCode;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SquadQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private DSLContext dsl;

  private String testSquadCode;
  private String testProjectCode;

  @BeforeEach
  void setUp() {
    String domainName = "CS";
    String clientCode = "ka_2";
    String projectCode = "ka_2_1";
    testProjectCode = projectCode;
    String squadCode = "ka_2_1_1";
    testSquadCode = squadCode;
    String employeeId = "12345678";

    // 1. 도메인 저장
    dsl.insertInto(DOMAIN).set(DOMAIN.NAME, domainName).onDuplicateKeyIgnore().execute();

    // 2. 클라이언트 회사 저장
    dsl.insertInto(CLIENT_COMPANY)
        .set(CLIENT_COMPANY.CLIENT_CODE, clientCode)
        .set(CLIENT_COMPANY.COMPANY_NAME, "카카오페이")
        .set(CLIENT_COMPANY.DOMAIN_NAME, domainName)
        .onDuplicateKeyIgnore()
        .execute();

    // 3. 프로젝트 저장
    dsl.insertInto(PROJECT)
        .set(PROJECT.PROJECT_CODE, projectCode)
        .set(PROJECT.CLIENT_CODE, clientCode)
        .set(PROJECT.TITLE, "테스트 프로젝트")
        .set(PROJECT.DESCRIPTION, "통합 테스트용")
        .set(PROJECT.START_DATE, LocalDate.of(2025, 1, 1))
        .set(PROJECT.EXPECTED_END_DATE, LocalDate.of(2025, 12, 31))
        .set(PROJECT.NUMBER_OF_MEMBERS, 1)
        .set(PROJECT.BUDGET, 10_000_000L)
        .set(PROJECT.STATUS, com.example.jooq.generated.enums.ProjectStatus.WAITING)
        .set(PROJECT.REQUEST_SPECIFICATION_URL, "http://example.com/spec")
        .set(PROJECT.DOMAIN_NAME, domainName)
        .execute();

    // 4. 멤버 저장
    dsl.insertInto(MEMBER)
        .set(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER, employeeId)
        .set(MEMBER.EMPLOYEE_NAME, "홍길동")
        .set(MEMBER.PASSWORD, "encoded-password")
        .set(MEMBER.PHONE_NUMBER, "01012345678")
        .set(MEMBER.POSITION_NAME, "과장")
        .set(MEMBER.DEPARTMENT_NAME, "개발팀")
        .set(MEMBER.BIRTHDAY, LocalDate.of(1995, 5, 5))
        .set(MEMBER.JOINED_AT, LocalDate.of(2020, 1, 1))
        .set(MEMBER.EMAIL, "hong@example.com")
        .set(MEMBER.CAREER_YEARS, 3)
        .set(MEMBER.SALARY, 50_000_000L)
        .set(MEMBER.STATUS, com.example.jooq.generated.enums.MemberStatus.AVAILABLE)
        .set(MEMBER.GRADE_CODE, MemberGradeCode.B)
        .set(MEMBER.ROLE, com.example.jooq.generated.enums.MemberRole.INSIDER)
        .execute();

    // 5. project_and_job 저장
    Long projectAndJobId =
        dsl.insertInto(PROJECT_AND_JOB)
            .set(PROJECT_AND_JOB.PROJECT_CODE, projectCode)
            .set(PROJECT_AND_JOB.JOB_NAME, "백엔드")
            .set(PROJECT_AND_JOB.REQUIRED_NUMBER, 1)
            .returning(PROJECT_AND_JOB.PROJECT_AND_JOB_ID)
            .fetchOne()
            .getProjectAndJobId();

    // 6. job_and_tech_stack 저장
    dsl.insertInto(JOB_AND_TECH_STACK)
        .set(JOB_AND_TECH_STACK.JOB_AND_TECH_STACK_ID, 99999L)
        .set(JOB_AND_TECH_STACK.PROJECT_AND_JOB_ID, projectAndJobId)
        .set(JOB_AND_TECH_STACK.TECH_STACK_NAME, "Java")
        .set(JOB_AND_TECH_STACK.PRIORITY, 1)
        .execute();

    // 7. squad 저장
    dsl.insertInto(SQUAD)
        .set(SQUAD.SQUAD_CODE, squadCode)
        .set(SQUAD.PROJECT_CODE, projectCode)
        .set(SQUAD.TITLE, "백엔드 스쿼드")
        .set(SQUAD.DESCRIPTION, "테스트 스쿼드")
        .set(SQUAD.IS_ACTIVE, (byte) 1)
        .set(SQUAD.ESTIMATED_COST, BigDecimal.valueOf(10_000_000L))
        .set(SQUAD.ESTIMATED_DURATION, BigDecimal.valueOf(12L))
        .set(SQUAD.ORIGIN_TYPE, com.example.jooq.generated.enums.SquadOriginType.MANUAL)
        .set(SQUAD.RECOMMENDATION_REASON, "직접 구성됨")
        .set(SQUAD.CREATED_AT, LocalDateTime.now())
        .set(SQUAD.UPDATED_AT, LocalDateTime.now())
        .execute();

    // 8. squad_employee 저장
    dsl.insertInto(SQUAD_EMPLOYEE)
        .set(SQUAD_EMPLOYEE.SQUAD_CODE, squadCode)
        .set(SQUAD_EMPLOYEE.EMPLOYEE_IDENTIFICATION_NUMBER, employeeId)
        .set(SQUAD_EMPLOYEE.PROJECT_AND_JOB_ID, projectAndJobId)
        .execute();
  }

  @Test
  @DisplayName("스쿼드 상세 조회 API는 정상 응답을 반환한다.")
  void getSquadDetail_success() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/squads/{squadCode}", testSquadCode)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.squadCode").value("ka_2_1_1"))
        .andExpect(jsonPath("$.data.title").value("백엔드 스쿼드"))
        .andExpect(jsonPath("$.data.recommendationReason").value("직접 구성됨"))
        .andExpect(jsonPath("$.data.totalMemberCount").value(1))
        .andExpect(jsonPath("$.data.memberCountByJob.백엔드").value(1))
        .andExpect(jsonPath("$.data.gradeCount.B").value(1))
        .andExpect(jsonPath("$.data.techStacks[0]").value("Java"))
        .andExpect(jsonPath("$.data.estimatedDuration").value(12))
        .andExpect(jsonPath("$.data.totalCost").value(10_000_000))
        .andExpect(jsonPath("$.data.description").value("테스트 스쿼드"))
        .andExpect(jsonPath("$.data.origin").value("MANUAL"))
        .andExpect(jsonPath("$.data.isActive").value(true))
        .andExpect(jsonPath("$.data.projectCode").value("ka_2_1"))
        .andExpect(jsonPath("$.data.members").isArray())
        .andExpect(jsonPath("$.data.members[0].name").value("홍길동"))
        .andExpect(jsonPath("$.data.members[0].job").value("백엔드"))
        .andExpect(jsonPath("$.data.members[0].grade").value("B"))
        .andExpect(jsonPath("$.data.members[0].monthlyUnitPrice").value(4500000)) // B등급 기준 추정
        .andExpect(jsonPath("$.data.members[0].memberId").value("12345678"))
        .andExpect(jsonPath("$.data.members[0].productivity").value(1.0));
  }

  @Test
  @DisplayName("프로젝트 코드로 스쿼드 목록을 조회한다")
  @WithMockUser(username = "testuser")
  void getSquadsByProjectCode_success() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/squads/project/{projectCode}", testProjectCode)
                .param("page", "0")
                .param("size", "10"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.content").isArray())
        .andExpect(jsonPath("$.data.content[0].squadCode").value(testSquadCode))
        .andExpect(jsonPath("$.data.content[0].squadName").value("백엔드 스쿼드"))
        .andExpect(jsonPath("$.data.content[0].aiRecommended").value(false))
        .andExpect(jsonPath("$.data.content[0].estimatedPeriod").isString())
        .andExpect(jsonPath("$.data.content[0].estimatedCost").isString())
        .andExpect(jsonPath("$.data.content[0].members").isArray())
        .andExpect(jsonPath("$.data.content[0].members[0].name").value("홍길동"))
        .andExpect(jsonPath("$.data.content[0].members[0].job").value("백엔드"))
        .andExpect(jsonPath("$.data.totalElements").value(1))
        .andExpect(jsonPath("$.data.currentPage").value(0))
        .andExpect(jsonPath("$.data.pageSize").value(10))
        .andExpect(jsonPath("$.data.hasNext").value(false))
        .andExpect(jsonPath("$.data.hasPrevious").value(false));
  }

  @Test
  @DisplayName("직무별 스쿼드 후보 추천 API는 정상 응답을 반환한다.")
  @WithMockUser
  void getCandidatesByRoles_success() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/squads/candidates")
                .param("projectId", testProjectCode)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.candidates").isMap())
        .andExpect(jsonPath("$.data.candidates.백엔드").isArray())
        .andExpect(jsonPath("$.data.candidates.백엔드[0].avgTechScore").isNumber())
        .andExpect(jsonPath("$.data.candidates.백엔드[0].domainCount").isNumber())
        .andExpect(jsonPath("$.data.candidates.백엔드[0].weight").isNumber())
        .andExpect(jsonPath("$.data.candidates.백엔드[0].monthlyUnitPrice").isNumber())
        .andExpect(jsonPath("$.data.candidates.백엔드[0].productivity").isNumber());
  }
}
