package com.nexus.sion.feature.member.query;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GradeQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Test
  @DisplayName("GET /api/v1/grades - 등급 조회 성공 (JPA로 insert, JOOQ로 조회)")
  void getUnitPriceByGrade_success() throws Exception {

    // when : 기존 데이터가 이미 존재한다는 전제
    mockMvc
        .perform(get("/api/v1/grades"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data").isArray())
        .andExpect(jsonPath("$.data", not(empty())))
        .andExpect(jsonPath("$.data[0].gradeCode").exists())
        .andExpect(jsonPath("$.data[0].monthlyUnitPrice").exists());
  }
}
