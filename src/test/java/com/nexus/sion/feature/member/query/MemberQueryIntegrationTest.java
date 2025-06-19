package com.nexus.sion.feature.member.query;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MemberQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @DisplayName("회원 목록을 정상적으로 조회한다")
  @WithMockUser(username = "testuser")
  @Test
  void getAllMembers_success() throws Exception {
    mockMvc
            .perform(
                    get("/members")
                            .param("page", "0")
                            .param("size", "10")
                            .param("status", "AVAILABLE"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content", not(empty())))
            .andExpect(jsonPath("$.data.totalElements", greaterThan(0)));
  }
}

