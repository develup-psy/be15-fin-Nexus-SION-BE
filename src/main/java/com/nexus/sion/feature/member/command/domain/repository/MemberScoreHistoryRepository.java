package com.nexus.sion.feature.member.command.domain.repository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.MemberScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberScoreHistoryRepository extends JpaRepository<MemberScoreHistory, Long> {

    Optional<MemberScoreHistory> findByEmployeeIdentificationNumber(String employeeIdentificationNumber);
}
