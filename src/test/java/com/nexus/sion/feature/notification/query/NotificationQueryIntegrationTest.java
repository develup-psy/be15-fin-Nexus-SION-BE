package com.nexus.sion.feature.notification.query;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.notification.query.dto.NotificationDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationQueryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("내 알림 목록 조회 - 200 OK & 데이터 구조 확인")
    @Test
    @WithMockUser(username = "testUser") // @AuthenticationPrincipal 모킹
    void testGetNotificationList() throws Exception {
        mockMvc.perform(get("/api/v1/notifications/me")
                        .param("page", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").exists())
                .andExpect(jsonPath("$.data.content").isArray());
    }

    @DisplayName("전체 알림 목록 조회 - 200 OK & 데이터 구조 확인")
    @Test
    void testGetAllNotificationList() throws Exception {
        mockMvc.perform(get("/api/v1/notifications")
                        .param("page", "0")
                        .param("size", "5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalElements").exists())
                .andExpect(jsonPath("$.data.content").isArray());
    }
}
