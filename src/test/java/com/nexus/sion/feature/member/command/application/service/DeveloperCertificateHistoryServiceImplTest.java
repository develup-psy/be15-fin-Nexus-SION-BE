package com.nexus.sion.feature.member.command.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;

import com.nexus.sion.common.s3.dto.S3UploadResponse;
import com.nexus.sion.common.s3.service.DocumentS3Service;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.UserCertificateHistoryRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Certificate;
import com.nexus.sion.feature.member.command.domain.repository.CertificateRepository;
import com.nexus.sion.feature.member.command.domain.repository.UserCertificateHistoryRepository;
import com.nexus.sion.feature.member.command.domain.repository.MemberRepository;

class DeveloperCertificateHistoryServiceImplTest {

  @InjectMocks
  private DeveloperCertificateHistoryServiceImpl service;

  @Mock
  private CertificateRepository certificateRepository;

  @Mock
  private UserCertificateHistoryRepository userCertificateHistoryRepository;

  @Mock
  private DocumentS3Service documentS3Service;

  @Mock
  private MemberRepository memberRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("자격증 등록 성공")
  void registerUserCertificate_success() {
    // given
    String employeeId = "DEV123";
    MockMultipartFile pdfFile =
            new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy".getBytes());

    UserCertificateHistoryRequest request =
            UserCertificateHistoryRequest.builder()
                    .certificateName("정보처리기사")
                    .issuingOrganization("한국산업인력공단")
                    .issueDate(LocalDate.of(2022, 5, 1))
                    .pdfFileUrl(pdfFile)
                    .build();

    Certificate certificate =
            Certificate.builder()
                    .certificateName("정보처리기사")
                    .issuingOrganization("한국산업인력공단")
                    .score(10)
                    .build();

    given(certificateRepository.findById("정보처리기사")).willReturn(Optional.of(certificate));
    given(documentS3Service.uploadFile(any(), any()))
            .willReturn(
                    new S3UploadResponse(
                            "https://s3.aws.com/certificates/test.pdf", "uuid.pdf", "test.pdf"));

    // when
    service.registerUserCertificate(employeeId, request);

    // then
    verify(userCertificateHistoryRepository).save(any());
  }

  @Test
  @DisplayName("존재하지 않는 자격증 등록 시 예외 발생")
  void registerUserCertificate_certificateNotFound() {
    // given
    String employeeId = "DEV123";
    MockMultipartFile pdfFile =
            new MockMultipartFile("file", "test.pdf", "application/pdf", "dummy".getBytes());

    UserCertificateHistoryRequest request =
            UserCertificateHistoryRequest.builder()
                    .certificateName("비존재자격증")
                    .issuingOrganization("테스트기관")
                    .issueDate(LocalDate.of(2022, 5, 1))
                    .pdfFileUrl(pdfFile)
                    .build();

    given(certificateRepository.findById("비존재자격증")).willReturn(Optional.empty());

    // when & then
    assertThatThrownBy(() -> service.registerUserCertificate(employeeId, request))
            .isInstanceOf(BusinessException.class)
            .hasMessage(ErrorCode.CERTIFICATE_NOT_FOUND.getMessage());

    verify(userCertificateHistoryRepository, never()).save(any());
  }
}
