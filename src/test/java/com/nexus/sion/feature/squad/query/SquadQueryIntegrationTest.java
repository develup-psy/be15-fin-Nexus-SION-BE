package com.nexus.sion.feature.squad.query;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SquadQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @DisplayName("프로젝트 코드로 스쿼드 목록을 조회한다")
  @WithMockUser(username = "testuser")
  @Test
  void getSquadsByProjectCode_success() throws Exception {
    // given
    String projectCode = "ha_1_1"; // 테스트용 프로젝트 코드 (사전 데이터 필요)

    // when & then
    mockMvc
        .perform(get("/api/v1/squads/project/{projectCode}", projectCode))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", not(empty())))
        .andExpect(jsonPath("$[0].squadCode", not(emptyOrNullString())))
        .andExpect(jsonPath("$[0].squadName", not(emptyOrNullString())))
        .andExpect(jsonPath("$[0].members", not(empty())))
        .andExpect(jsonPath("$[0].members[0].name", not(emptyOrNullString())))
        .andExpect(jsonPath("$[0].members[0].job", not(emptyOrNullString())));
  }
}
