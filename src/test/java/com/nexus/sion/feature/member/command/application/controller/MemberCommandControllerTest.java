package com.nexus.sion.feature.member.command.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.member.command.application.dto.request.UserCreateRequest;
import com.nexus.sion.feature.member.command.application.service.UserCommandService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(controllers = MemberCommandController.class)
@Import(MemberCommandControllerTest.SecurityTestConfig.class)
class MemberCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserCommandService userCommandService;


    @TestConfiguration
    static class MockConfig {
        @Bean
        public UserCommandService userCommandService() {
            return mock(UserCommandService.class);
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

    @Test
    void 회원가입_성공() throws Exception {
        // given
        UserCreateRequest request = new UserCreateRequest(
                "EMP123",
                "홍길동",
                "test@example.com",
                "Password123!"
        );

        String requestJson = objectMapper.writeValueAsString(request);

        // when & then
        mockMvc.perform(post("/api/v1/members/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());

        // verify: 서비스가 한 번 호출됐는지 확인
        verify(userCommandService).registerUser(any(UserCreateRequest.class));
    }
}

