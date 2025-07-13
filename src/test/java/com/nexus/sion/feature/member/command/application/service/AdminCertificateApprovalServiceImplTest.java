package com.nexus.sion.feature.member.command.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.command.application.dto.request.CertificateRejectRequest;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.UserCertificateHistory;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.CertificateStatus;
import com.nexus.sion.feature.member.command.domain.repository.UserCertificateHistoryRepository;
import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class AdminCertificateApprovalServiceImplTest {

    @InjectMocks
    private AdminCertificateApprovalServiceImpl service;

    @Mock
    private UserCertificateHistoryRepository historyRepository;

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
        given(historyRepository.findById(id)).willReturn(Optional.of(history));

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
        CertificateRejectRequest request = new CertificateRejectRequest("파일 훼손");
        UserCertificateHistory history = mock(UserCertificateHistory.class);
        given(historyRepository.findById(id)).willReturn(Optional.of(history));

        // when
        service.rejectUserCertificate(id, request);

        // then
        then(history).should().reject("파일 훼손");
        then(historyRepository).should().save(history);
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
