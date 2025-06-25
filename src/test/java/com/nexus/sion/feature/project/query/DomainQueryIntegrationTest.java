package com.nexus.sion.feature.project.query;

import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.nexus.sion.feature.project.command.domain.aggregate.Domain;
import com.nexus.sion.feature.project.command.repository.DomainRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DomainQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private DomainRepository domainRepository;

  @Test
  void 도메인_전체조회_성공() throws Exception {
    // 테스트 데이터 저장
    String domainName = "test";

    domainRepository.save(Domain.of(domainName));
    domainRepository.flush();

    // when & then
    mockMvc
        .perform(get("/api/v1/domains").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.domains", hasItems(domainName)));
  }
}
