package com.nexus.sion.feature.member.query.repository;

import static com.example.jooq.generated.tables.Certificate.CERTIFICATE;
import static com.example.jooq.generated.tables.Member.MEMBER;
import static com.example.jooq.generated.tables.UserCertificateHistory.USER_CERTIFICATE_HISTORY;

import java.util.List;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import com.example.jooq.generated.enums.UserCertificateHistoryCertificateStatus;
import com.nexus.sion.feature.member.query.dto.response.UserCertificateHistoryResponse;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserCertificateHistoryQueryRepository {

  private final DSLContext dsl;

  public List<UserCertificateHistoryResponse> findByMemberId(String memberId) {
    return dsl.select(
            USER_CERTIFICATE_HISTORY.USER_CERTIFICATE_HISTORY_ID,
            CERTIFICATE.CERTIFICATE_NAME,
            CERTIFICATE.ISSUING_ORGANIZATION,
            MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER,
            MEMBER.EMPLOYEE_NAME,
            USER_CERTIFICATE_HISTORY.ISSUE_DATE,
            USER_CERTIFICATE_HISTORY.PDF_FILE_URL,
            USER_CERTIFICATE_HISTORY.CERTIFICATE_STATUS,
            USER_CERTIFICATE_HISTORY.REJECTED_REASON,
            USER_CERTIFICATE_HISTORY.CREATED_AT,
            USER_CERTIFICATE_HISTORY.UPDATED_AT)
        .from(USER_CERTIFICATE_HISTORY)
        .join(CERTIFICATE)
        .on(USER_CERTIFICATE_HISTORY.CERTIFICATE_NAME.eq(CERTIFICATE.CERTIFICATE_NAME))
        .join(MEMBER)
        .on(
            USER_CERTIFICATE_HISTORY.EMPLOYEE_IDENTIFICATION_NUMBER.eq(
                MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
        .where(
            USER_CERTIFICATE_HISTORY.EMPLOYEE_IDENTIFICATION_NUMBER.eq(
                MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
        .fetch(
            record ->
                UserCertificateHistoryResponse.builder()
                    .userCertificateHistoryId(
                        record.get(USER_CERTIFICATE_HISTORY.USER_CERTIFICATE_HISTORY_ID))
                    .certificateName(record.get(CERTIFICATE.CERTIFICATE_NAME))
                    .issuingOrganization(record.get(CERTIFICATE.ISSUING_ORGANIZATION))
                    .employeeIdentificationNumber(record.get(MEMBER.EMPLOYEE_IDENTIFICATION_NUMBER))
                    .employeeName(record.get(MEMBER.EMPLOYEE_NAME))
                    .issueDate(record.get(USER_CERTIFICATE_HISTORY.ISSUE_DATE).toLocalDate())
                    .pdfFileUrl(record.get(USER_CERTIFICATE_HISTORY.PDF_FILE_URL))
                    .certificateStatus(
                        record.get(USER_CERTIFICATE_HISTORY.CERTIFICATE_STATUS).name())
                    .rejectedReason(record.get(USER_CERTIFICATE_HISTORY.REJECTED_REASON))
                    .createdAt(record.get(USER_CERTIFICATE_HISTORY.CREATED_AT))
                    .updatedAt(record.get(USER_CERTIFICATE_HISTORY.UPDATED_AT))
                    .build());
  }

  public List<String> findAllCertificateNames() {
    return dsl.selectDistinct(CERTIFICATE.CERTIFICATE_NAME)
        .from(CERTIFICATE)
        .fetchInto(String.class);
  }

  public List<String> findOwnedCertificateNamesByStatus(
      String employeeId, UserCertificateHistoryCertificateStatus status) {
    return dsl.selectDistinct(USER_CERTIFICATE_HISTORY.CERTIFICATE_NAME)
        .from(USER_CERTIFICATE_HISTORY)
        .where(
            USER_CERTIFICATE_HISTORY
                .EMPLOYEE_IDENTIFICATION_NUMBER
                .eq(employeeId)
                .and(USER_CERTIFICATE_HISTORY.CERTIFICATE_STATUS.eq(status)))
        .fetchInto(String.class);
  }
}
