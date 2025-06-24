package com.nexus.sion.feature.project.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.project.command.application.dto.request.DomainRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.Domain;
import com.nexus.sion.feature.project.command.repository.DomainRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class DomainCommandIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private DomainRepository domainRepository;

  @Autowired private ObjectMapper objectMapper;

  @Test
  @DisplayName("새로운 도메인을 등록하면 201이 반환된다.")
  void registerNewDomain_returnsCreated() throws Exception {
    // given
    String domainName = "test";
    DomainRequest request = new DomainRequest(domainName);

    // when & then
    mockMvc
        .perform(
            post("/api/v1/domains")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated()); // 201 반환되는지 확인

    // then - DB에 저장되었는지 확인
    assertThat(domainRepository.findById(domainName)).isPresent();
  }

  @Test
  @DisplayName("이미 존재하는 도메인은 저장하지 않고, 200이 반환된다.")
  void registerExistingTechStack_doesNotSaveAgain() throws Exception {
    // given
    String existingDomainName = "techStackName";
    domainRepository.save(Domain.of(existingDomainName));
    int existingCount = domainRepository.findAll().size();

    DomainRequest request = new DomainRequest(existingDomainName);

    // when & then
    mockMvc
        .perform(
            post("/api/v1/domains")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk()); // 200 반환되는지 확인

    // then - 여전히 하나만 존재
    assertThat(domainRepository.findAll().size()).isEqualTo(existingCount);
  }
}
