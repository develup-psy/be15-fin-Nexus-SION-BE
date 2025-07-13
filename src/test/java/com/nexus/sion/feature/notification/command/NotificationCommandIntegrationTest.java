package com.nexus.sion.feature.notification.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.notification.command.application.dto.request.SquadShareNotificationRequest;
import com.nexus.sion.feature.notification.command.domain.aggregate.NotificationType;
import com.nexus.sion.feature.notification.command.domain.repository.NotificationRepository;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NotificationCommandIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private NotificationRepository notificationRepository;

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void 알림_전체_읽기_API_성공() throws Exception {
        mockMvc
                .perform(patch("/api/v1/notifications/reads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "EMP002", roles = {"ADMIN"})
    void 알림_단건_읽기_API_성공() throws Exception {
        // given: 알림 저장
        var saved = notificationRepository.save(
                NotificationType.SQUAD_COMMENT.toEntity("EMP001", "EMP002", "테스트 알림", "content123"));

        // when + then
        mockMvc
                .perform(patch("/api/v1/notifications/reads/{id}", saved.getNotificationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void 알림_단건_읽기_API_실패_존재하지_않는_ID() throws Exception {
        mockMvc
                .perform(patch("/api/v1/notifications/reads/999999"))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("90001"));
    }

    @Test
    @WithMockUser(username = "EMP003", roles = {"ADMIN"})
    void 스쿼드_공유_알림_API_성공() throws Exception {
        // given
        SquadShareNotificationRequest request = SquadShareNotificationRequest.builder()
        .receivers(List.of("EMP001", "EMP002"))
        .squadCode("SQUAD001")
                .build();

        String json = objectMapper.writeValueAsString(request);

        // when + then
        mockMvc
                .perform(
                        post("/api/v1/notifications/squad-share")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void 인증되지_않은_사용자_구독_요청_실패() throws Exception {
        mockMvc
                .perform(get("/api/v1/notifications/connect"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "testUser", roles = {"USER"})
    void SSE_구독_API_성공() throws Exception {
        mockMvc
                .perform(get("/api/v1/notifications/connect"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/event-stream"));
    }
}