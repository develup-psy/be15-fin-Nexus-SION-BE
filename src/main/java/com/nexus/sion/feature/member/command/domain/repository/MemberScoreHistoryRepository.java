package com.nexus.sion.feature.member.command.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.MemberScoreHistory;

public interface MemberScoreHistoryRepository extends JpaRepository<MemberScoreHistory, Long> {

  Optional<MemberScoreHistory> findByEmployeeIdentificationNumber(
      String employeeIdentificationNumber);

  Optional<MemberScoreHistory> findTopByEmployeeIdentificationNumberOrderByCreatedAtDesc(String employeeIdentificationNumber);
}

