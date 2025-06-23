package com.nexus.sion.feature.member.command.domain.repository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.InitialScore;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InitialScoreRepository extends JpaRepository<InitialScore, String> {
    Optional<InitialScore> findByYears(int years);

    Optional<InitialScore> findTopByYearsLessThanEqualOrderByYearsDesc(int years);
}