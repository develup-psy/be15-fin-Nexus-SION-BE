package com.nexus.sion.feature.member.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.jooq.generated.enums.UserCertificateHistoryCertificateStatus;
import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;
import com.nexus.sion.feature.member.query.repository.UserCertificateHistoryQueryRepository;

class UserCertificateHistoryQueryServiceImplTest {

  private UserCertificateHistoryQueryRepository userCertificateHistoryQueryRepository;
  private UserCertificateHistoryQueryServiceImpl userCertificateHistoryQueryService;

  @BeforeEach
  void setUp() {
    userCertificateHistoryQueryRepository = mock(UserCertificateHistoryQueryRepository.class);
    userCertificateHistoryQueryService =
        new UserCertificateHistoryQueryServiceImpl(userCertificateHistoryQueryRepository);
  }

  @Test
  void getMyCertificates_shouldReturnCertificateList() {
    // given
    String memberId = "EMP001";
    List<UserCertificateHistoryResponse> mockResponse =
        List.of(
            UserCertificateHistoryResponse.builder()
                .userCertificateHistoryId(1L)
                .certificateName("정보처리기사")
                .issuingOrganization("한국산업인력공단")
                .employeeIdentificationNumber("EMP001")
                .pdfFileUrl("url1")
                .certificateStatus("APPROVED")
                .build());
    when(userCertificateHistoryQueryRepository.findByMemberId(memberId)).thenReturn(mockResponse);

    // when
    List<UserCertificateHistoryResponse> result =
        userCertificateHistoryQueryService.getMyCertificates(memberId);

    // then
    assertThat(result).isEqualTo(mockResponse);
    verify(userCertificateHistoryQueryRepository).findByMemberId(memberId);
  }

  @Test
  void findAllCertificateNames_shouldReturnNameList() {
    // given
    List<String> names = List.of("정보처리기사", "SQLD");
    when(userCertificateHistoryQueryRepository.findAllCertificateNames()).thenReturn(names);

    // when
    List<String> result = userCertificateHistoryQueryService.findAllCertificateNames();

    // then
    assertThat(result).containsExactlyElementsOf(names);
    verify(userCertificateHistoryQueryRepository).findAllCertificateNames();
  }

  @Test
  void findOwnedCertificateNamesByEmployee_shouldReturnApprovedCertificateNames() {
    // given
    String employeeId = "EMP001";
    List<String> owned = List.of("정보처리기사");
    when(userCertificateHistoryQueryRepository.findOwnedCertificateNamesByStatus(
            employeeId, UserCertificateHistoryCertificateStatus.APPROVED))
        .thenReturn(owned);

    // when
    List<String> result =
        userCertificateHistoryQueryService.findOwnedCertificateNamesByEmployee(employeeId);

    // then
    assertThat(result).isEqualTo(owned);
    verify(userCertificateHistoryQueryRepository)
        .findOwnedCertificateNamesByStatus(
            employeeId, UserCertificateHistoryCertificateStatus.APPROVED);
  }
}
