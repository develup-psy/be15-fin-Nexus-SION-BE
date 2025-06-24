package com.nexus.sion.feature.project.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.exception.ErrorCode;
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
    DomainRequest request = DomainRequest.builder().name(domainName).build();

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
  void registerExistingDomain_doesNotSaveAgain() throws Exception {
    // given
    String existingDomainName = "domainName";
    domainRepository.save(Domain.of(existingDomainName));
    int existingCount = domainRepository.findAll().size();

    DomainRequest request = DomainRequest.builder().name(existingDomainName).build();

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

  @Test
  @DisplayName("기술 스택을 삭제하면 201이 반환되고 DB에서 제거된다.")
  void deleteExistingDomain_returnsDeleted() throws Exception {
    // given
    String domainName = "test";
    domainRepository.save(Domain.of(domainName));

    // when & then
    mockMvc
        .perform(delete("/api/v1/domains/{domainName}", domainName))
        .andExpect(status().isNoContent());

    // then: DB에서 해당 기술 스택이 제거되었는지 확인한다.
    assertThat(domainRepository.findById(domainName)).isNotPresent();
  }

  @Test
  @DisplayName("존재하지 않는 기술 스택은 에러를 반환한다.")
  void deleteExistingDomain_returnsError() throws Exception {
    // given
    String domainName = "test";

    // when & then
    mockMvc
        .perform(delete("/api/v1/domains/{domainName}", domainName))
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.DOMAIN_NOT_FOUND.getCode()))
        .andExpect(jsonPath("$.message").value(ErrorCode.DOMAIN_NOT_FOUND.getMessage()))
        .andExpect(jsonPath("$.timestamp").exists());
  }
}
