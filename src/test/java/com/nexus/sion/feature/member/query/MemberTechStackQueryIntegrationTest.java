package com.nexus.sion.feature.member.query;

import static com.example.jooq.generated.tables.DeveloperTechStack.DEVELOPER_TECH_STACK;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.example.jooq.generated.enums.MemberRole;
import com.example.jooq.generated.enums.MemberStatus;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberTechStackQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private DSLContext dsl;

  private String employeeId;

  @BeforeEach
  void setUp() {
    // member insert
    employeeId =
        dsl.insertInto(MEMBER)
            .set(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER, "EMP001")
            .set(MEMBER.EMPLOYEE_NAME, "홍길동")
            .set(MEMBER.EMAIL, "hong@test.com")
            .set(MEMBER.ROLE, MemberRole.INSIDER)
            .set(MEMBER.STATUS, MemberStatus.AVAILABLE)
            .set(MEMBER.PASSWORD, "test1234!")
            .set(MEMBER.PHONE_NUMBER, "01012345678")
            .set(MEMBER.CREATED_AT, LocalDateTime.now())
            .returning(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER)
            .fetchOne()
            .getEmployeeIdentificationNumber();

    // developer_tech_stack insert
    dsl.insertInto(DEVELOPER_TECH_STACK)
        .set(DEVELOPER_TECH_STACK.DEVELOPER_TECH_STACK_ID, 999L)
        .set(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER, employeeId)
        .set(DEVELOPER_TECH_STACK.TECH_STACK_NAME, "java")
        .set(DEVELOPER_TECH_STACK.TECH_STACK_TOTAL_SCORES, 90)
        .execute();

    dsl.insertInto(DEVELOPER_TECH_STACK)
        .set(DEVELOPER_TECH_STACK.DEVELOPER_TECH_STACK_ID, 1000L)
        .set(DEVELOPER_TECH_STACK.EMPLOYEE_IDENTIFICATION_NUMBER, employeeId)
        .set(DEVELOPER_TECH_STACK.TECH_STACK_NAME, "spring")
        .set(DEVELOPER_TECH_STACK.TECH_STACK_TOTAL_SCORES, 80)
        .execute();
  }

  @DisplayName("사번으로 기술스택을 조회한다")
  @WithMockUser
  @Test
  void getTechStacks_success() throws Exception {
    mockMvc
        .perform(get("/api/v1/members/{employeeId}/tech-stacks", "EMP001"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data", hasSize(2)))
        .andExpect(jsonPath("$.data[0].techStackName", notNullValue()))
        .andExpect(jsonPath("$.data[0].score", notNullValue()));
  }
}
