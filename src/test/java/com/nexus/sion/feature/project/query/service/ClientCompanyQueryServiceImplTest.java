package com.nexus.sion.feature.project.query.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nexus.sion.common.dto.PageResponse;
import com.nexus.sion.feature.project.query.dto.request.ClientCompanySearchRequest;
import com.nexus.sion.feature.project.query.dto.response.ClientCompanyDto;
import com.nexus.sion.feature.project.query.repository.ClientCompanyQueryRepository;

@ExtendWith(MockitoExtension.class)
class ClientCompanyQueryServiceImplTest {

  @Mock private ClientCompanyQueryRepository clientCompanyQueryRepository;

  @InjectMocks private ClientCompanyQueryServiceImpl clientCompanyQueryService;

  @Test
  @DisplayName("고객사 목록 페이징 조회에 성공한다")
  void findClientCompanies_success() {
    // given
    int page = 0;
    int size = 10;

    ClientCompanySearchRequest request = new ClientCompanySearchRequest();
    request.setPage(page);
    request.setSize(size);
    request.setCompanyName("company"); // 필터링 조건

    List<ClientCompanyDto> mockCompanyList =
        List.of(
            ClientCompanyDto.builder()
                .clientCode("test1")
                .companyName("company1")
                .domainName("domain1")
                .build(),
            ClientCompanyDto.builder()
                .clientCode("test2")
                .companyName("company2")
                .domainName("domain2")
                .build());

    long totalCount = 2L;

    when(clientCompanyQueryRepository.countByCondition(any())).thenReturn(totalCount);
    when(clientCompanyQueryRepository.findClientCompaniesByCondition(
            any(), any(), eq(page), eq(size)))
        .thenReturn(mockCompanyList);

    // when
    PageResponse<ClientCompanyDto> response =
        clientCompanyQueryService.findClientCompanies(request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getTotalElements()).isEqualTo(totalCount);
    assertThat(response.getContent()).hasSize(2);
    assertThat(response.getContent())
        .extracting(ClientCompanyDto::getCompanyName)
        .containsExactly("company1", "company2");

    verify(clientCompanyQueryRepository).countByCondition(any());
    verify(clientCompanyQueryRepository)
        .findClientCompaniesByCondition(any(), any(), eq(page), eq(size));
  }

  @Test
  @DisplayName("companyName 없이 고객사 전체 조회에 성공한다")
  void findClientCompanies_withoutCompanyName_success() {
    // given
    int page = 0;
    int size = 5;

    ClientCompanySearchRequest request = new ClientCompanySearchRequest();
    request.setPage(page);
    request.setSize(size);
    request.setCompanyName(""); // 비어있는 조건

    List<ClientCompanyDto> mockList =
        List.of(
            ClientCompanyDto.builder().clientCode("a1").companyName("Alpha").build(),
            ClientCompanyDto.builder().clientCode("b1").companyName("Beta").build());

    when(clientCompanyQueryRepository.countByCondition(any())).thenReturn(2L);
    when(clientCompanyQueryRepository.findClientCompaniesByCondition(
            isNull(), any(), eq(page), eq(size)))
        .thenReturn(mockList);

    // when
    PageResponse<ClientCompanyDto> response =
        clientCompanyQueryService.findClientCompanies(request);

    // then
    assertThat(response.getTotalElements()).isEqualTo(2L);
    assertThat(response.getContent()).hasSize(2);
    verify(clientCompanyQueryRepository).countByCondition(isNull());
    verify(clientCompanyQueryRepository)
        .findClientCompaniesByCondition(isNull(), any(), eq(page), eq(size));
  }

  @Test
  @DisplayName("2페이지 고객사 목록 조회에 성공한다")
  void findClientCompanies_withPage2_success() {
    // given
    int page = 2;
    int size = 10;

    ClientCompanySearchRequest request = new ClientCompanySearchRequest();
    request.setPage(page);
    request.setSize(size);
    request.setCompanyName("test");

    List<ClientCompanyDto> mockList =
        List.of(ClientCompanyDto.builder().clientCode("t1").companyName("test1").build());

    when(clientCompanyQueryRepository.countByCondition(any())).thenReturn(11L);
    when(clientCompanyQueryRepository.findClientCompaniesByCondition(
            any(), any(), eq(page), eq(size)))
        .thenReturn(mockList);

    // when
    PageResponse<ClientCompanyDto> response =
        clientCompanyQueryService.findClientCompanies(request);

    // then
    assertThat(response.getTotalElements()).isEqualTo(11L);
    assertThat(response.getContent()).hasSize(1);
    verify(clientCompanyQueryRepository).countByCondition(any());
    verify(clientCompanyQueryRepository)
        .findClientCompaniesByCondition(any(), any(), eq(page), eq(size));
  }
}
