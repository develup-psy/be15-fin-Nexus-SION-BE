package com.nexus.sion.feature.member.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.member.command.application.dto.request.InitialScoreDto;
import com.nexus.sion.feature.member.command.application.dto.request.InitialScoreSetRequest;
import com.nexus.sion.feature.member.command.domain.repository.InitialScoreRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class InitialScoreCommandIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private InitialScoreRepository initialScoreRepository;

  @Autowired private ObjectMapper objectMapper;

  @Test
  void setInitialScores_success() throws Exception {
    // given
    List<InitialScoreDto> scores =
        List.of(
            InitialScoreDto.builder().minYears(1).maxYears(3).score(50).build(),
            InitialScoreDto.builder().minYears(4).maxYears(6).score(70).build(),
            InitialScoreDto.builder().minYears(7).maxYears(null).score(90).build());
    InitialScoreSetRequest request = InitialScoreSetRequest.builder().initialScores(scores).build();

    String jsonRequest = objectMapper.writeValueAsString(request);

    // when & then
    mockMvc
        .perform(
            post("/api/v1/initial-scores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andExpect(status().isOk());

    // DB 저장 확인
    var savedScores = initialScoreRepository.findAll();
    assertThat(savedScores).hasSize(3);

    assertThat(savedScores.get(0).getMinYears()).isEqualTo(1);
    assertThat(savedScores.get(0).getMaxYears()).isEqualTo(3);
    assertThat(savedScores.get(0).getScore()).isEqualTo(50);

    assertThat(savedScores.get(2).getMaxYears()).isNull();
  }
}
