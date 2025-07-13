package com.nexus.sion.feature.member.query.service;

import com.nexus.sion.feature.member.command.domain.repository.CertificateRepository;
import com.nexus.sion.feature.member.query.dto.response.CertificateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

class CertificateQueryServiceImplTest {

    @Mock
    private CertificateRepository certificateRepository;

    @InjectMocks
    private CertificateQueryServiceImpl certificateQueryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("자격증 전체 조회 성공")
    void getAllCertificates_success() {
        // given
        List<CertificateResponse> mockResponse = List.of(
                new CertificateResponse("정보처리기사", 10, "한국산업인력공단"),
                new CertificateResponse("SQLD", 5, "한국데이터산업진흥원")
        );
        given(certificateRepository.findAllAsResponse()).willReturn(mockResponse);

        // when
        List<CertificateResponse> result = certificateQueryService.getAllCertificates();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).certificateName()).isEqualTo("정보처리기사");
        assertThat(result.get(1).issuingOrganization()).isEqualTo("한국데이터산업진흥원");
    }
}
