package com.nexus.sion.feature.squad.query;

import static com.example.jooq.generated.Tables.SQUAD_COMMENT;
import static com.example.jooq.generated.tables.Squad.SQUAD;
import static com.example.jooq.generated.tables.SquadEmployee.SQUAD_EMPLOYEE;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.jooq.generated.enums.SquadOriginType;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SquadQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private DSLContext dsl;

  private String testSquadCode;

  @BeforeEach
  void setUp() {
    testSquadCode = "ha_1_1_1";
    String projectCode = "ha_1_1";

    // 자식 테이블 먼저 삭제
    dsl.deleteFrom(SQUAD_EMPLOYEE).where(SQUAD_EMPLOYEE.SQUAD_CODE.eq(testSquadCode)).execute();
    dsl.deleteFrom(SQUAD_COMMENT).where(SQUAD_COMMENT.SQUAD_CODE.eq(testSquadCode)).execute();
    dsl.deleteFrom(SQUAD).where(SQUAD.SQUAD_CODE.eq(testSquadCode)).execute();

    // 스쿼드 더미 데이터 삽입
    dsl.insertInto(SQUAD)
        .set(SQUAD.SQUAD_CODE, testSquadCode)
        .set(SQUAD.PROJECT_CODE, projectCode)
        .set(SQUAD.TITLE, "백엔드 스쿼드")
        .set(SQUAD.DESCRIPTION, "테스트용 스쿼드")
        .set(SQUAD.IS_ACTIVE, (byte) 1)
        .set(SQUAD.CREATED_AT, LocalDateTime.now())
        .set(SQUAD.UPDATED_AT, LocalDateTime.now())
        .set(SQUAD.ESTIMATED_DURATION, new BigDecimal("3.0"))
        .set(SQUAD.ESTIMATED_COST, new BigDecimal("3000000.00"))
        .set(SQUAD.ORIGIN_TYPE, SquadOriginType.AI)
        .set(SQUAD.RECOMMENDATION_REASON, "직접 구성됨")
        .execute();

    // 참고: 추후 member, grade, squad_employee 등 관련 테이블도 더미 추가 필요
  }

  @Test
  @DisplayName("스쿼드 상세 조회 API는 정상 응답을 반환한다.")
  void getSquadDetail_success() throws Exception {
    mockMvc
        .perform(get("/api/v1/squads/" + testSquadCode).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.squadCode").value(testSquadCode))
        .andExpect(jsonPath("$.squadName").value("백엔드 스쿼드"))
        .andExpect(jsonPath("$.aiRecommended").value(true))
        .andExpect(jsonPath("$.estimatedPeriod").exists())
        .andExpect(jsonPath("$.estimatedCost").exists())
        .andExpect(jsonPath("$.members").isArray())
        .andExpect(jsonPath("$.techStacks").isArray())
        .andExpect(jsonPath("$.costDetails").isArray())
        .andExpect(jsonPath("$.summary.jobCounts").exists())
        .andExpect(jsonPath("$.summary.gradeCounts").exists());
  }

  @Test
  @DisplayName("프로젝트 코드로 스쿼드 목록을 조회한다")
  @WithMockUser(username = "testuser")
  void getSquadsByProjectCode_success() throws Exception {
    String projectCode = "ha_1_1";

    mockMvc
        .perform(get("/api/v1/squads/project/{projectCode}", projectCode))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", not(empty())))
        .andExpect(jsonPath("$[0].squadCode", not(emptyOrNullString())))
        .andExpect(jsonPath("$[0].squadName", not(emptyOrNullString())));
  }
}
