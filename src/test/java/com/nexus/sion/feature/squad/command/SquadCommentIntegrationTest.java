package com.nexus.sion.feature.squad.command;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadCommentRegisterRequest;
import com.nexus.sion.feature.squad.command.repository.SquadCommentRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SquadCommentCommandIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private SquadCommentRepository squadCommentRepository;

  @Test
  @DisplayName("코멘트 등록에 성공한다")
  void registerComment_success() throws Exception {
    // given
    String squadCode = "ha_1_1_1"; // 실제 DB에 존재하는 코드일 것
    SquadCommentRegisterRequest request =
        new SquadCommentRegisterRequest("EMP001", "스쿼드에 대한 코멘트입니다.");

    // when & then
    mockMvc
        .perform(
            post("/api/v1/squads/{squadCode}/comments", squadCode)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("코멘트 내용이 공백이면 400 에러를 반환한다")
  void registerComment_fail_whenContentIsBlank() throws Exception {
    String squadCode = "ha_1_1_1";
    SquadCommentRegisterRequest request = new SquadCommentRegisterRequest("EMP001", " ");

    mockMvc
        .perform(
            post("/api/v1/squads/{squadCode}/comments", squadCode)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }
}
