package com.nexus.sion.feature.member.command.application.controller;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.nexus.sion.feature.member.command.application.dto.request.MemberCreateRequest;
import com.nexus.sion.feature.member.command.application.service.MemberCommandService;

@AutoConfigureMockMvc
@WebMvcTest(controllers = MemberCommandController.class)
@Import(MemberCommandControllerTest.SecurityTestConfig.class)
class MemberCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberCommandService userCommandService;

    @Autowired
    private MemberCommandService memberCommandService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public MemberCommandService userCommandService() {
            return mock(MemberCommandService.class);
        }
    }

    @TestConfiguration
    static class SecurityTestConfig {
        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(AbstractHttpConfigurer::disable);
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
            return http.build();
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("회원가입 통합 테스트")
    @Test
    void 회원가입_성공() throws Exception {
        MemberCreateRequest request = MemberCreateRequest.builder().email("test@example.com")
                        .password("Test1234!").employeeName("테스트유저").phoneNumber("01012345678")
                        .build();

        mockMvc.perform(post("/api/v1/members/signup").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.success").value(true)).andDo(print());
    }
}
