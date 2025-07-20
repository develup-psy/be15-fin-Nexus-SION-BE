package com.nexus.sion.feature.member.query.service;

import com.example.jooq.generated.tables.records.MemberScoreHistoryRecord;
import com.nexus.sion.exception.BusinessException;
import com.nexus.sion.exception.ErrorCode;
import com.nexus.sion.feature.member.query.dto.response.MemberScoreHistoryResponse;
import com.nexus.sion.feature.member.query.repository.MemberScoreQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class MemberScoreHistoryQueryServiceImplTest {

    @Mock
    private MemberScoreQueryRepository repository;

    @InjectMocks
    private MemberScoreHistoryQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("회원 점수 이력 조회 - 정상 케이스")
    void getScoreHistory_success() {
        // given
        String employeeId = "E001";
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime prevDate = now.minusDays(1);

        MemberScoreHistoryRecord curr = new MemberScoreHistoryRecord();
        curr.setEmployeeIdentificationNumber(employeeId);
        curr.setTotalTechStackScores(60);
        curr.setTotalCertificateScores(40);
        curr.setCreatedAt(now);

        MemberScoreHistoryRecord prevTech = new MemberScoreHistoryRecord();
        prevTech.setTotalTechStackScores(50);
        prevTech.setTotalCertificateScores(35);
        prevTech.setCreatedAt(prevDate);

        MemberScoreHistoryRecord prevCert = new MemberScoreHistoryRecord();
        prevCert.setTotalTechStackScores(45);
        prevCert.setTotalCertificateScores(30);
        prevCert.setCreatedAt(prevDate.minusDays(1));

        given(repository.getLatestRecord(employeeId)).willReturn(curr);
        given(repository.getPreviousTechScoreChangedRecord(employeeId, 60)).willReturn(prevTech);
        given(repository.getPreviousCertificateScoreChangedRecord(employeeId, 40)).willReturn(prevCert);

        // when
        MemberScoreHistoryResponse result = service.getScoreHistory(employeeId);

        // then
        assertThat(result.employeeIdentificationNumber()).isEqualTo(employeeId);
        assertThat(result.currentTechScore()).isEqualTo(60);
        assertThat(result.currentCertificateScore()).isEqualTo(40);
        assertThat(result.currentTotalScore()).isEqualTo(100);
        assertThat(result.previousTechScore()).isEqualTo(50);
        assertThat(result.previousCertificateScore()).isEqualTo(30);
        assertThat(result.previousTotalScore()).isEqualTo(85); // prevTech 사용
    }

    @Test
    @DisplayName("회원 점수 이력 조회 - 사용자 없음")
    void getScoreHistory_userNotFound() {
        // given
        String employeeId = "E002";
        given(repository.getLatestRecord(employeeId)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> service.getScoreHistory(employeeId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }
}
