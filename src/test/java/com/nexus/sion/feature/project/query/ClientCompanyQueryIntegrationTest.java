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
            .perform(get("/api/v1/client-companies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("page", "0")
                    .param("size", "10")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            // content에 test1, test2 포함 여부만 확인 (총 개수는 변수화 불가)
            .andExpect(jsonPath("$.data.content[*].clientCode", hasItems("test1", "test2")))
            .andExpect(jsonPath("$.data.content[*].companyName", hasItems("company1", "company2")))
            .andExpect(jsonPath("$.data.content[*].domainName", hasItems("domain1")));
  }

  @Test
  @DisplayName("회사명 일부로만 조회 - 통합 테스트")
  void getLikeClientCompanies() throws Exception {
    mockMvc
            .perform(get("/api/v1/client-companies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("page", "0")
                    .param("size", "10")
                    .param("companyName", "company")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            // content에 test1, test2 포함 여부만 확인 (총 개수는 변수화 불가)
            .andExpect(jsonPath("$.data.content[*].clientCode", hasItems("test1", "test2")))
            .andExpect(jsonPath("$.data.content[*].companyName", hasItems("company1", "company2")))
            .andExpect(jsonPath("$.data.content[*].domainName", everyItem(is("domain1"))));
  }

  @Test
  @DisplayName("회사명으로 필터링된 고객사 조회 - 통합 테스트")
  void getClientCompaniesByCompanyName() throws Exception {
    mockMvc
            .perform(get("/api/v1/client-companies")
                    .contentType(MediaType.APPLICATION_JSON)
                    .param("page", "0")
                    .param("size", "10")
                    .param("companyName", "company1")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.totalElements").value(1))
            .andExpect(jsonPath("$.data.content.length()").value(1))
            .andExpect(jsonPath("$.data.content[0].clientCode").value("test1"))
            .andExpect(jsonPath("$.data.content[0].companyName").value("company1"))
            .andExpect(jsonPath("$.data.content[0].domainName").value("domain1"));
  }
}
