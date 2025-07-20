package com.nexus.sion.feature.member.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Certificate;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.MemberScoreHistory;
import com.nexus.sion.feature.member.command.domain.repository.CertificateRepository;
import com.nexus.sion.feature.member.command.domain.repository.MemberScoreHistoryRepository;
import com.nexus.sion.feature.notification.command.application.service.NotificationCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.CertificateRejectRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.UserCertificateHistory;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.CertificateStatus;
import com.nexus.sion.feature.member.command.domain.repository.UserCertificateHistoryRepository;
import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;

class AdminCertificateApprovalServiceImplTest {

  @InjectMocks private AdminCertificateApprovalServiceImpl service;

  @Mock private UserCertificateHistoryRepository historyRepository;

  @Mock
  private NotificationCommandService notificationCommandService;

  @Mock
  private CertificateRepository certificateRepository;

  @Mock
  private MemberScoreHistoryRepository memberScoreHistoryRepository;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  @DisplayName("전체 자격증 목록 조회 성공")
  void getAllCertificates_success() {
    // given
    UserCertificateHistory entity = mock(UserCertificateHistory.class);
    given(entity.getCertificateStatus()).willReturn(CertificateStatus.APPROVED);
    given(historyRepository.findAll()).willReturn(List.of(entity));

    // when
    List<UserCertificateHistoryResponse> result = service.getAllCertificates();

    // then
    then(historyRepository).should(times(1)).findAll();
    assertThat(result).hasSize(1);
  }

  @Test
  @DisplayName("자격증 승인 성공")
  void approveUserCertificate_success() {
    // given
    Long id = 1L;
    UserCertificateHistory history = mock(UserCertificateHistory.class);
    Certificate certificate = mock(Certificate.class);
    MemberScoreHistory scoreHistory = mock(MemberScoreHistory.class);

    given(historyRepository.findById(id)).willReturn(Optional.of(history));
    given(certificateRepository.findById(any())).willReturn(Optional.of(certificate));
    given(memberScoreHistoryRepository.findTopByEmployeeIdentificationNumberOrderByCreatedAtDesc(any()))
            .willReturn(Optional.of(scoreHistory));

    // when
    service.approveUserCertificate(id);

    // then
    then(history).should().approve();
    then(historyRepository).should().save(history);
  }


  @Test
  @DisplayName("자격증 승인 실패 - 존재하지 않는 ID")
  void approveUserCertificate_fail_notFound() {
    // given
    Long id = 99L;
    given(historyRepository.findById(id)).willReturn(Optional.empty());

    // expect
    assertThatThrownBy(() -> service.approveUserCertificate(id))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.USER_CERTIFICATE_NOT_FOUND.getMessage());

    then(historyRepository).should(never()).save(any());
  }

  @Test
  @DisplayName("자격증 반려 성공")
  void rejectUserCertificate_success() {
    // given
    Long id = 2L;
    String reason = "파일 훼손";
    CertificateRejectRequest request = new CertificateRejectRequest(reason);
    UserCertificateHistory history = mock(UserCertificateHistory.class);
    given(historyRepository.findById(id)).willReturn(Optional.of(history));

    // when
    service.rejectUserCertificate(id, request);

    // then
    then(history).should().reject(reason);
    then(historyRepository).should().save(history);
    then(notificationCommandService).should().createAndSendNotification(
            any(), any(), any(), any(), any()
    );
  }

  @Test
  @DisplayName("자격증 반려 실패 - 존재하지 않는 ID")
  void rejectUserCertificate_fail_notFound() {
    // given
    Long id = 999L;
    CertificateRejectRequest request = new CertificateRejectRequest("유효하지 않음");
    given(historyRepository.findById(id)).willReturn(Optional.empty());

    // expect
    assertThatThrownBy(() -> service.rejectUserCertificate(id, request))
        .isInstanceOf(BusinessException.class)
        .hasMessage(ErrorCode.USER_CERTIFICATE_NOT_FOUND.getMessage());

    then(historyRepository).should(never()).save(any());
  }
}
