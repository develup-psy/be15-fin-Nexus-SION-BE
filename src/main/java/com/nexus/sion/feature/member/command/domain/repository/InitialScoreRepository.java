package com.nexus.sion.feature.member.command.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.InitialScore;

public interface InitialScoreRepository extends JpaRepository<InitialScore, String> {
  Optional<InitialScore> findByYears(int years);

  Optional<InitialScore> findTopByYearsLessThanEqualOrderByYearsDesc(int years);
}
