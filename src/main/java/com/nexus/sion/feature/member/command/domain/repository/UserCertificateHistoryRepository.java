package com.nexus.sion.feature.member.command.domain.repository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.UserCertificateHistory;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.CertificateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserCertificateHistoryRepository extends JpaRepository<UserCertificateHistory, Long> {

    // 특정 사번의 이력 조회
    List<UserCertificateHistory> findByEmployeeIdentificationNumber(String employeeId);

    // 상태별 이력 조회
    List<UserCertificateHistory> findByCertificateStatus(CertificateStatus status);
}
