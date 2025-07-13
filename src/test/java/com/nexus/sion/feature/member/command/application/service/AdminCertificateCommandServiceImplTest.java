package com.nexus.sion.feature.member.command.application.service;

import com.nexus.sion.feature.member.command.application.dto.request.CertificateCreateRequest;
import com.nexus.sion.feature.member.command.application.dto.request.CertificateUpdateRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Certificate;
import com.nexus.sion.feature.member.command.domain.repository.CertificateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class AdminCertificateCommandServiceImplTest {

    @Mock
    private CertificateRepository certificateRepository;

    @InjectMocks
    private AdminCertificateCommandServiceImpl certificateCommandService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("자격증 등록 성공")
    void registerCertificate_success() {
        // given
        CertificateCreateRequest request = new CertificateCreateRequest("정보처리기사", "한국산업인력공단", 10);

        // when
        certificateCommandService.registerCertificate(request);

        // then
        verify(certificateRepository).save(any(Certificate.class));
    }

    @Test
    @DisplayName("자격증 수정 성공")
    void updateCertificate_success() {
        // given
        String certName = "정보처리기사";
        Certificate certificate = Certificate.builder()
                .certificateName(certName)
                .issuingOrganization("기존기관")
                .score(5)
                .build();

        CertificateUpdateRequest request = new CertificateUpdateRequest("한국산업인력공단", 10);

        given(certificateRepository.findById(certName)).willReturn(Optional.of(certificate));

        // when
        certificateCommandService.updateCertificate(certName, request);

        // then
        assertThat(certificate.getScore()).isEqualTo(10);
        assertThat(certificate.getIssuingOrganization()).isEqualTo("한국산업인력공단");
    }

    @Test
    @DisplayName("자격증 삭제 성공")
    void deleteCertificate_success() {
        // given
        String certName = "정보처리기사";
        Certificate certificate = Certificate.builder()
                .certificateName(certName)
                .issuingOrganization("기관")
                .score(10)
                .build();

        given(certificateRepository.findById(certName)).willReturn(Optional.of(certificate));

        // when
        certificateCommandService.deleteCertificate(certName);

        // then
        verify(certificateRepository).delete(certificate);
    }
}
