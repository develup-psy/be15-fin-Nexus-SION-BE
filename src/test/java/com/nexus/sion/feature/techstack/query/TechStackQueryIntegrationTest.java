package com.nexus.sion.feature.techstack.query;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.feature.techstack.command.domain.aggregate.TechStack;
import com.nexus.sion.feature.techstack.command.repository.TechStackRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TechStackQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private TechStackRepository techStackRepository;

  @Test
  void 기술스택_전체조회_성공() throws Exception {
    // 테스트 데이터 저장
    String techStackName = "test";

    techStackRepository.save(TechStack.of(techStackName));
    techStackRepository.flush();

    // when & then
    mockMvc
        .perform(get("/api/v1/tech-stack").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.techStacks", hasItems(techStackName)));
  }

  @Test
  @DisplayName("기술스택 자동완성 검색 성공")
  void techstack_autocomplete_search_success() throws Exception {
    // given
    techStackRepository.save(TechStack.of("Spring Boot"));
    techStackRepository.save(TechStack.of("Spring Security"));
    techStackRepository.save(TechStack.of("Spring Batch"));
    techStackRepository.flush();

    // when & then
    mockMvc.perform(get("/api/v1/tech-stack/autocomplete")
                    .param("keyword", "Spr")
                    .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.techStacks", hasItems("Spring Boot", "Spring Security", "Spring Batch")));
  }


}
