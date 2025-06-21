package com.nexus.sion.feature.statistics.query.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.statistics.query.dto.DeveloperDto;
import com.nexus.sion.feature.statistics.query.dto.TechStackCountDto;
import com.nexus.sion.feature.statistics.query.service.StatisticsQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = StatisticsQueryController.class)
@Import(StatisticsQueryControllerTest.TestSecurityConfig.class)
class StatisticsQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsQueryService service;

    @Autowired
    private ObjectMapper objectMapper;

    // ✅ Security 무력화를 위한 내부 설정 클래스
    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            http.csrf().disable()
                    .authorizeHttpRequests().anyRequest().permitAll();
            return http.build();
        }
    }

    @Test
    @DisplayName("기술스택 전체 목록 조회 성공")
    void getAllTechStacks_success() throws Exception {
        List<String> stacks = List.of("Java", "Spring Boot");
        when(service.findAllStackNames()).thenReturn(stacks);

        mockMvc.perform(get("/api/v1/statistics/all-tech-stacks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0]").value("Java"));
    }

    @Test
    @DisplayName("스택별 개발자 수 조회 성공")
    void getStackMemberCount_success() throws Exception {
        List<String> stacks = List.of("Java", "Vue.js");
        List<TechStackCountDto> result = List.of(
                new TechStackCountDto("Java", 5),
                new TechStackCountDto("Vue.js", 3)
        );

        when(service.getStackMemberCounts(stacks)).thenReturn(result);

        mockMvc.perform(post("/api/v1/statistics/stack/member-count")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stacks)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].techStackName").value("Java"))
                .andExpect(jsonPath("$.data[1].techStackName").value("Vue.js"));
    }

    @Test
    @DisplayName("개발자 목록 조회 성공")
    void getAllDevelopers_success() throws Exception {
        List<DeveloperDto> devs = List.of(
                DeveloperDto.builder()
                        .name("홍길동")
                        .position("사원")
                        .department("개발팀")
                        .code("EMP001")
                        .grade("B")
                        .status("AVAILABLE")
                        .techStacks(List.of("Java", "Vue.js"))
                        .build()
        );

        PageResponse<DeveloperDto> page = PageResponse.fromJooq(devs, 1, 0, 10);

        when(service.getAllDevelopers(1, 10)).thenReturn(page);

        mockMvc.perform(get("/api/v1/statistics/developers?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("홍길동"));
    }
}
