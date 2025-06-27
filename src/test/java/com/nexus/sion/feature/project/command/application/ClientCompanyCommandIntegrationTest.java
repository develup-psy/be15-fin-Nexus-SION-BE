package com.nexus.sion.feature.project.command.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
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
import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyCreateRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyUpdateRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.ClientCompany;
import com.nexus.sion.feature.project.command.domain.aggregate.Domain;
import com.nexus.sion.feature.project.command.repository.ClientCompanyRepository;
import com.nexus.sion.feature.project.command.repository.DomainRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ClientCompanyCommandIntegrationTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ClientCompanyRepository clientCompanyRepository;

  @Autowired private DomainRepository domainRepository;

  @Autowired private ObjectMapper objectMapper;

  String existingDomainName = "domainName";
  String clientCode = "TEST_CLIENT";

  @BeforeEach
  void setUp() {
    domainRepository.save(Domain.of(existingDomainName));
    domainRepository.flush();

    ClientCompany clientCompany =
        ClientCompany.builder()
            .clientCode(clientCode)
            .companyName("Old Company")
            .domainName(existingDomainName)
            .email("old@email.com")
            .build();

    clientCompanyRepository.save(clientCompany);
    clientCompanyRepository.flush();
  }

  @Test
  @DisplayName("새로운 고객사를 등록하면 201이 반환된다.")
  void registerNewClientCompany_returnsCreated() throws Exception {
    // given
    String companyName = "회사이름";
    ClientCompanyCreateRequest request =
        ClientCompanyCreateRequest.builder()
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
    ClientCompanyCreateRequest request =
        ClientCompanyCreateRequest.builder()
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
        .andExpect(status().isConflict()) // <-- 409 기대
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorCode").value("30006"))
        .andExpect(jsonPath("$.message").value("이미 존재하는 고객사 코드입니다."));

    // then - 여전히 하나만 존재
    assertThat(clientCompanyRepository.count()).isEqualTo(existingCount);
  }

  @Test
  @DisplayName("고객사 정보 수정 성공")
  void updateClientCompanySuccess() throws Exception {
    // given
    ClientCompanyUpdateRequest request =
        ClientCompanyUpdateRequest.builder()
            .companyName("New Company")
            .email("new@email.com")
            .build();

    // when
    mockMvc
        .perform(
            patch("/api/v1/client-companies/{clientCode}", clientCode)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));

    // then
    ClientCompany updated = clientCompanyRepository.findById(clientCode).orElseThrow();
    assertThat(updated.getCompanyName()).isEqualTo("New Company");
    assertThat(updated.getEmail()).isEqualTo("new@email.com");
  }

  @Test
  @DisplayName("존재하지 않는 고객사 수정 시 실패")
  void updateClientCompanyNotFound() throws Exception {
    ClientCompanyUpdateRequest request =
        ClientCompanyUpdateRequest.builder().companyName("Nothing").email("none@email.com").build();

    long existingCount = clientCompanyRepository.count();

    mockMvc
        .perform(
            patch("/api/v1/client-companies/{clientCode}", "NOT_EXIST")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isNotFound()) // <-- 404 기대
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorCode").value("30009"))
        .andExpect(jsonPath("$.message").value("해당 고객사가 존재하지 않습니다."));

    // then - 여전히 하나만 존재
    assertThat(clientCompanyRepository.count()).isEqualTo(existingCount);
  }

  @Test
  @DisplayName("유효하지 않은 요청값이면 실패")
  void updateClientCompanyValidationFail() throws Exception {
    ClientCompanyUpdateRequest request =
        ClientCompanyUpdateRequest.builder()
            .companyName("") // NotBlank 유효성 실패 가정
            .email("invalid-email") // 이메일 형식 실패 가정
            .build();

    mockMvc
        .perform(
            patch("/api/v1/client-companies/{clientCode}", clientCode)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("고객사를 삭제하면 204가 반환되고 DB에서 제거된다.")
  void deleteExistingClientCompany_returnsDeleted() throws Exception {
    // given

    // when & then
    mockMvc
        .perform(delete("/api/v1/client-companies/{clientCode}", clientCode))
        .andExpect(status().isNoContent());

    // then: DB에서 해당 기술 스택이 제거되었는지 확인한다.
    assertThat(domainRepository.findById(clientCode)).isNotPresent();
  }

  @Test
  @DisplayName("존재하지 않는 고객사는 에러를 반환한다.")
  void deleteExistingClientCode_returnsError() throws Exception {
    // given
    String clientCode = "test";

    // when & then
    mockMvc
        .perform(delete("/api/v1/client-companies/{clientCode}", clientCode))
        .andExpect(status().is4xxClientError())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.errorCode").value(ErrorCode.CLIENT_COMPANY_NOT_FOUND.getCode()))
        .andExpect(jsonPath("$.message").value(ErrorCode.CLIENT_COMPANY_NOT_FOUND.getMessage()))
        .andExpect(jsonPath("$.timestamp").exists());
  }
}
