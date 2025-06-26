package com.nexus.sion.feature.project.command.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyCreateRequest;
import com.nexus.sion.feature.project.command.application.dto.request.ClientCompanyUpdateRequest;
import com.nexus.sion.feature.project.command.domain.aggregate.ClientCompany;
import com.nexus.sion.feature.project.command.repository.ClientCompanyRepository;

@ExtendWith(MockitoExtension.class)
class ClientCompanyCommandServiceImplTest {
  @InjectMocks private ClientCompanyCommandServiceImpl service;

  @Mock private ModelMapper modelMapper;

  @Mock private ClientCompanyRepository clientCompanyRepository;

  @Test
  void registerClientCompany_success() {
    // given
    ClientCompanyCreateRequest request =
        ClientCompanyCreateRequest.builder()
            .companyName("나이스")
            .email("test@example.com")
            .contactNumber("01012345678")
            .build();

    ClientCompany mappedEntity = new ClientCompany();
    when(modelMapper.map(eq(request), eq(ClientCompany.class))).thenReturn(mappedEntity);
    when(clientCompanyRepository.findTopByClientCodeStartingWithOrderByClientCodeDesc(
            "나이".toLowerCase() + "_"))
        .thenReturn(Optional.empty());

    // when
    service.registerClientCompany(request);

    // then
    assertNotNull(mappedEntity.getClientCode());
    verify(clientCompanyRepository).save(mappedEntity);
  }

  @Test
  void registerClientCompany_이미존재하면예외발생() {
    // given
    String companyName = "회사이름";
    ClientCompanyCreateRequest request =
        ClientCompanyCreateRequest.builder().companyName(companyName).domainName("도메인이름").build();
    when(clientCompanyRepository.existsByCompanyName(companyName)).thenReturn(true);

    // when
    BusinessException exception =
        assertThrows(
            BusinessException.class,
            () -> {
              service.registerClientCompany(request);
            });

    // then
    assertEquals(ErrorCode.CLIENT_COMPANY_ALREADY_EXIST, exception.getErrorCode());
    verify(clientCompanyRepository, never()).save(any(ClientCompany.class));
  }

  @Test
  void registerClientCompany_이메일형식_잘못되었을때_예외발생() {
    // given
    ClientCompanyCreateRequest request =
        ClientCompanyCreateRequest.builder().companyName("나이스").email("invalid-email").build();

    // when & then
    BusinessException ex =
        assertThrows(BusinessException.class, () -> service.registerClientCompany(request));
    assertEquals(ErrorCode.INVALID_EMAIL_FORMAT, ex.getErrorCode());
    verify(clientCompanyRepository, never()).save(any());
  }

  @Test
  void registerClientCompany_전화번호형식_잘못되었을때_예외발생() {
    // given
    ClientCompanyCreateRequest request =
        ClientCompanyCreateRequest.builder()
            .companyName("나이스")
            .email("test@example.com")
            .contactNumber("12345")
            .build();

    // when & then
    BusinessException ex =
        assertThrows(BusinessException.class, () -> service.registerClientCompany(request));
    assertEquals(ErrorCode.INVALID_PHONE_NUMBER_FORMAT, ex.getErrorCode());
    verify(clientCompanyRepository, never()).save(any());
  }

  @Test
  void generateClientCode_기존코드있을때_숫자증가() {
    // given
    ClientCompanyCreateRequest request =
        ClientCompanyCreateRequest.builder().companyName("나이스").build();

    String codePrefix = "나이".toLowerCase() + "_";
    ClientCompany existing = new ClientCompany();
    existing.setClientCode("나이_004");

    when(modelMapper.map(any(), eq(ClientCompany.class))).thenReturn(new ClientCompany());
    when(clientCompanyRepository.findTopByClientCodeStartingWithOrderByClientCodeDesc(codePrefix))
        .thenReturn(Optional.of(existing));

    // when
    service.registerClientCompany(request);

    // then
    ArgumentCaptor<ClientCompany> captor = ArgumentCaptor.forClass(ClientCompany.class);
    verify(clientCompanyRepository).save(captor.capture());

    ClientCompany saved = captor.getValue();
    assertEquals("나이_005", saved.getClientCode());
  }

  @Test
  void generateClientCode_숫자파싱실패시_예외() {
    // given
    ClientCompanyCreateRequest request =
        ClientCompanyCreateRequest.builder().companyName("나이스").build();

    ClientCompany existing = new ClientCompany();
    existing.setClientCode("나이_abc"); // 숫자가 아님

    when(clientCompanyRepository.findTopByClientCodeStartingWithOrderByClientCodeDesc(
            "나이".toLowerCase() + "_"))
        .thenReturn(Optional.of(existing));

    // when & then
    BusinessException ex =
        assertThrows(BusinessException.class, () -> service.registerClientCompany(request));
    assertEquals(ErrorCode.INVALID_CLIENT_CODE_FORMAT, ex.getErrorCode());
  }

  // 고객사 정보 업데이트 테스트 코드
  ClientCompany getExistingClientCompany(String clientCode) {
    return ClientCompany.builder()
        .clientCode(clientCode)
        .companyName("Old")
        .domainName("old.com")
        .contactPerson("Old Person")
        .email("old@email.com")
        .contactNumber("010-0000-0000")
        .build();
  }

  ClientCompanyUpdateRequest getUpdateRequest() {
    return ClientCompanyUpdateRequest.builder()
        .companyName("New Company")
        .domainName("new.com")
        .contactPerson("New Person")
        .email("new@email.com")
        .contactNumber("010-1234-5678")
        .build();
  }

  @Test
  @DisplayName("고객사 정보 업데이트 성공")
  void updateClientCompany_success() {
    // given
    String clientCode = "co_001";
    ClientCompany existing = getExistingClientCompany(clientCode);
    ClientCompanyUpdateRequest request = getUpdateRequest();

    when(clientCompanyRepository.findById(clientCode)).thenReturn(Optional.of(existing));

    // when
    service.updateClientCompany(request, clientCode);

    // then
    assertThat(existing.getCompanyName()).isEqualTo("New Company");
    assertThat(existing.getDomainName()).isEqualTo("new.com");
    assertThat(existing.getContactPerson()).isEqualTo("New Person");
    assertThat(existing.getEmail()).isEqualTo("new@email.com");
    assertThat(existing.getContactNumber()).isEqualTo("010-1234-5678");

    verify(clientCompanyRepository).findById(clientCode);
  }

  @Test
  @DisplayName("고객사가 존재하지 않을 때 예외 발생")
  void updateClientCompany_clientNotFound() {
    // given
    String clientCode = "not_exist_code";
    ClientCompanyUpdateRequest request =
        ClientCompanyUpdateRequest.builder().companyName("New Name").build();

    when(clientCompanyRepository.findById(clientCode)).thenReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> service.updateClientCompany(request, clientCode))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining(ErrorCode.CLIENT_COMPANY_NOT_FOUND.getMessage());

    verify(clientCompanyRepository).findById(clientCode);
  }

  @Test
  @DisplayName("모든 필드가 유효한 값으로 업데이트되는 경우")
  void update_allFieldsChanged() {
    // given
    ClientCompany clientCompany = getExistingClientCompany("co_001");
    ClientCompanyUpdateRequest request = getUpdateRequest();

    // when
    clientCompany.update(request);

    // then
    assertThat(clientCompany.getCompanyName()).isEqualTo("New Company");
    assertThat(clientCompany.getDomainName()).isEqualTo("new.com");
    assertThat(clientCompany.getContactPerson()).isEqualTo("New Person");
    assertThat(clientCompany.getEmail()).isEqualTo("new@email.com");
    assertThat(clientCompany.getContactNumber()).isEqualTo("010-1234-5678");
  }

  @Test
  @DisplayName("일부 필드만 업데이트되고 null은 무시되는 경우")
  void update_partialFieldsChanged_ignoreNulls() {
    // given
    ClientCompany clientCompany = getExistingClientCompany("co_002");
    ClientCompanyUpdateRequest request =
        ClientCompanyUpdateRequest.builder()
            .companyName(null)
            .domainName("updated.com")
            .contactPerson(null)
            .email("updated@email.com")
            .contactNumber(null)
            .build();

    // when
    clientCompany.update(request);

    // then
    assertThat(clientCompany.getCompanyName()).isEqualTo("Old"); // null 무시됨
    assertThat(clientCompany.getDomainName()).isEqualTo("updated.com");
    assertThat(clientCompany.getContactPerson()).isEqualTo("Old Person"); // null 무시됨
    assertThat(clientCompany.getEmail()).isEqualTo("updated@email.com");
    assertThat(clientCompany.getContactNumber()).isEqualTo("010-0000-0000"); // null 무시됨
  }


  @Test
  void deleteClientCompany_존재하면삭제() {
    // given
    String clientCode = "고객사코드";
    when(clientCompanyRepository.existsById(clientCode)).thenReturn(true);
    doNothing().when(clientCompanyRepository).deleteById(clientCode);

    // when
    service.deleteClientCompany(clientCode);

    // then
    verify(clientCompanyRepository, times(1)).deleteById(clientCode);
  }

  @Test
  void deleteDomain_존재하지않으면에러() {
    // given
    String clientCode = "고객사코드";
    when(clientCompanyRepository.existsById(clientCode)).thenReturn(false);

    // when & then
    BusinessException exception =
            assertThrows(
                    BusinessException.class,
                    () -> {
                      service.deleteClientCompany(clientCode);
                    });

    // then
    assertEquals(ErrorCode.CLIENT_COMPANY_NOT_FOUND, exception.getErrorCode());

    verify(clientCompanyRepository, never()).deleteById(any());
  }
}
