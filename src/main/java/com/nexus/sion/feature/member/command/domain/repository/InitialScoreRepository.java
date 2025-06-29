package com.nexus.sion.feature.member.command.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.InitialScore;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InitialScoreRepository extends JpaRepository<InitialScore, String> {
  @Query("SELECT i FROM InitialScore i " +
          "WHERE i.minYears <= :careerYears " +
          "AND (i.maxYears >= :careerYears OR i.maxYears IS NULL)")
  Optional<InitialScore> findByCareerYears(@Param("careerYears") int careerYears);
}
