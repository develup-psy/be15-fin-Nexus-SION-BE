package com.nexus.sion.feature.project.command.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyCreateRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.ClientCompany;
import com.nexus.sion.feature.project.command.domain.aggregate.Domain;
import com.nexus.sion.feature.project.command.repository.ClientCompanyRepository;
import com.nexus.sion.feature.project.command.repository.DomainRepository;
import com.nexus.sion.feature.techstack.command.application.dto.request.TechStackRequest;
import com.nexus.sion.feature.techstack.command.domain.aggregate.TechStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ClientCompanyCommandIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired private ClientCompanyRepository clientCompanyRepository;

    @Autowired private DomainRepository domainRepository;

    @Autowired private ObjectMapper objectMapper;

    String existingDomainName = "domainName";

    @BeforeEach
    void setUp() {
        domainRepository.save(Domain.of(existingDomainName));
        domainRepository.flush();
    }

    @Test
    @DisplayName("새로운 고객사를 등록하면 201이 반환된다.")
    void registerNewClientCompany_returnsCreated() throws Exception {
        // given
        String companyName = "회사이름";
        ClientCompanyCreateRequest request = ClientCompanyCreateRequest.builder()
                .companyName(companyName)
                .domainName(existingDomainName)
                .build();

        // when & then
        mockMvc
                .perform(
                        post("/api/v1/client-companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // then - DB에 저장되었는지 확인
        assertThat(clientCompanyRepository.existsByCompanyName(companyName)).isTrue();
    }

    @Test
    @DisplayName("이미 존재하는 고객사는 저장하지 않는다.")
    void registerExistingClientCompany_doesNotSaveAgain() throws Exception {
        // given
        ClientCompanyCreateRequest request = ClientCompanyCreateRequest.builder()
                .companyName("회사이름")
                .domainName(existingDomainName)
                .build();
        clientCompanyRepository.save(ClientCompany.of(request, 999));

        long existingCount = clientCompanyRepository.count();

        // when & then
        mockMvc
                .perform(
                        post("/api/v1/client-companies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk()); // 200 반환되는지 확인

        // then - 여전히 하나만 존재
        assertThat(clientCompanyRepository.count()).isEqualTo(existingCount);
    }

}
