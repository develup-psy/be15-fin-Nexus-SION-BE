package com.nexus.sion.feature.squad.query.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResponse;
import com.nexus.sion.feature.squad.query.service.SquadQueryService;

@WebMvcTest(SquadQueryController.class)
@AutoConfigureMockMvc
@Import(SquadQueryControllerTest.SecurityTestConfig.class)
class SquadQueryControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private SquadQueryService squadQueryService;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @TestConfiguration
  static class MockConfig {
    @Bean
    public SquadQueryService squadQueryService() {
      return mock(SquadQueryService.class);
    }
  }

  @TestConfiguration
  static class SecurityTestConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      http.csrf(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
      return http.build();
    }
  }

  @Test
  @DisplayName("프로젝트 코드로 스쿼드 리스트 조회 성공")
  void getSquads_Success() throws Exception {
    // given
    String projectCode = "ha_1_1";
    SquadListResponse.MemberInfo member = new SquadListResponse.MemberInfo("김개발", "Backend");
    SquadListResponse squad =
        new SquadListResponse(
            "ha_1_1_1", // squadCode
            "SQUAD 1", // squadName
            false, // isAiRecommended
            List.of(member), // members
            "3개월", // estimatedPeriod
            "1,000만원" // estimatedCost
            );

    SquadListRequest request = new SquadListRequest();
    request.setProjectCode(projectCode);

    PageResponse<SquadListResponse> response = PageResponse.fromJooq(List.of(squad), 10, 5, 1);

    when(squadQueryService.findSquads(any(SquadListRequest.class))).thenReturn(response);
    mockMvc
        .perform(
            get("/api/v1/squads/project/{projectCode}", projectCode)
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].squadCode").value("ha_1_1_1"))
        .andDo(print());
  }
}
