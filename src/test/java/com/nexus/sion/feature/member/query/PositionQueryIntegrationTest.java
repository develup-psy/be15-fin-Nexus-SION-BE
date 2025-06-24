package com.nexus.sion.feature.member.query;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Position;
import com.nexus.sion.feature.member.command.domain.repository.DeveloperTechStackRepository;
import com.nexus.sion.feature.member.command.domain.repository.PositionRepository;
import com.nexus.sion.feature.member.command.repository.MemberRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PositionQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private MemberRepository memberRepository;
  @Autowired private DeveloperTechStackRepository developerTechStackRepository;
  @Autowired private PositionRepository positionRepository;

  @BeforeEach
  void setUp() {
    developerTechStackRepository.deleteAll();
    memberRepository.deleteAll();

    if (!positionRepository.existsById("PM")) {
      positionRepository.save(Position.builder().positionName("PM").build());
    }

    positionRepository.flush();
  }

  @Test
  @DisplayName("직급 목록 조회 API - 응답에 'PM' 포함 확인")
  void getAllPositions_success_containsExpectedPositions() throws Exception {
    mockMvc
        .perform(get("/api/v1/positions").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data[*].positionName", hasItems("PM")));
  }
}
