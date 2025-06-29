package com.nexus.sion.feature.member.command.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.InitialScore;

public interface InitialScoreRepository extends JpaRepository<InitialScore, Long> {
  @Query(
      "SELECT i FROM InitialScore i "
          + "WHERE i.minYears <= :careerYears "
          + "AND (i.maxYears >= :careerYears OR i.maxYears IS NULL)")
  Optional<InitialScore> findByCareerYears(@Param("careerYears") int careerYears);
}
