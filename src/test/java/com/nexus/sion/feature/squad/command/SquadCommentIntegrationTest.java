package com.nexus.sion.feature.squad.command;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadCommentRegisterRequest;
import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadComment;
import com.nexus.sion.feature.squad.command.repository.SquadCommentRepository;

@SpringBootTest
@AutoConfigureMockMvc
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
    SquadCommentRegisterRequest request = new SquadCommentRegisterRequest("스쿼드에 대한 코멘트입니다.");

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
    SquadCommentRegisterRequest request = new SquadCommentRegisterRequest("");

    mockMvc
        .perform(
            post("/api/v1/squads/{squadCode}/comments", squadCode)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("코멘트 삭제에 성공한다")
  void deleteComment_success() throws Exception {
    // given
    SquadComment comment =
        SquadComment.builder()
            .squadCode("ha_1_1_1")
            .employeeIdentificationNumber("02202308")
            .content("삭제 테스트 코멘트입니다.")
            .build();
    squadCommentRepository.saveAndFlush(comment);

    // when & then
    mockMvc
        .perform(
            delete(
                "/api/v1/squads/{squadCode}/comments/{commentId}",
                comment.getSquadCode(),
                comment.getId()))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("코멘트가 해당 스쿼드에 속하지 않으면 삭제에 실패한다")
  void deleteComment_fail_whenSquadCodeMismatch() throws Exception {
    SquadComment comment =
        SquadComment.builder()
            .squadCode("ha_1_1_1")
            .employeeIdentificationNumber("02202308")
            .content("스쿼드 불일치 테스트")
            .build();
    squadCommentRepository.saveAndFlush(comment);

    // 다른 squadCode로 요청
    mockMvc
        .perform(delete("/api/v1/squads/{squadCode}/comments/{commentId}", "다른코드", comment.getId()))
        .andExpect(status().isForbidden());
  }
}
