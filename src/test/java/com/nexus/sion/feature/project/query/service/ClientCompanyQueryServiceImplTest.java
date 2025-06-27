package com.nexus.sion.feature.project.query.service;

import com.nexus.sion.feature.project.query.dto.response.ClientCompanyDto;
import com.nexus.sion.feature.project.query.dto.response.ClientCompanyListResponse;
import com.nexus.sion.feature.project.query.repository.ClientCompanyQueryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientCompanyQueryServiceImplTest {

    @Mock
    private ClientCompanyQueryRepository clientCompanyQueryRepository;

    @InjectMocks
    private ClientCompanyQueryServiceImpl clientCompanyQueryService;


    @Test
    @DisplayName("모든 고객사 목록 조회에 성공한다")
    void findAllClientCompany_success() {
        // given
        List<ClientCompanyDto> mockCompanyList = List.of(
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
        when(clientCompanyQueryRepository.findAllClientCompanies()).thenReturn(mockCompanyList);

        // when
        ClientCompanyListResponse response = clientCompanyQueryService.findAllClientCompany();

        // then
        assertThat(response).isNotNull();
        assertThat(response.clientCompanies()).hasSize(2);

        assertThat(response.clientCompanies())
                .extracting(ClientCompanyDto::getCompanyName)
                .containsExactly("company1", "company2");

        verify(clientCompanyQueryRepository, times(1)).findAllClientCompanies();
    }
}