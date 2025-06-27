package com.nexus.sion.feature.project.query;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.nexus.sion.feature.project.command.domain.aggregate.ClientCompany;
import com.nexus.sion.feature.project.command.domain.aggregate.Domain;
import com.nexus.sion.feature.project.command.repository.ClientCompanyRepository;
import com.nexus.sion.feature.project.command.repository.DomainRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ClientCompanyQueryIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ClientCompanyRepository clientCompanyRepository;
  @Autowired private DomainRepository domainRepository;

  @BeforeEach
  void setUp() {
    domainRepository.save(Domain.of("domain1"));
    domainRepository.flush();

    clientCompanyRepository.save(
        ClientCompany.builder()
            .clientCode("test1")
            .companyName("company1")
            .domainName("domain1")
            .build());

    clientCompanyRepository.save(
        ClientCompany.builder()
            .clientCode("test2")
            .companyName("company2")
            .domainName("domain1")
            .build());
    clientCompanyRepository.flush();
  }

  @Test
  @DisplayName("고객사 전체 조회 - 통합 테스트")
  void getAllClientCompanies() throws Exception {
    mockMvc
        .perform(get("/api/v1/client-companies").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(
            jsonPath("$.data.clientCompanies[*].clientCode").value(hasItems("test1", "test2")))
        .andExpect(
            jsonPath("$.data.clientCompanies[*].companyName")
                .value(hasItems("company1", "company2")))
        .andExpect(
            jsonPath("$.data.clientCompanies[*].domainName").value(hasItems("domain1", "domain1")));
  }
}
