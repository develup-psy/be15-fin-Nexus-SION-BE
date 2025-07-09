package com.nexus.sion.feature.squad.command.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;

public interface SquadCommandRepository extends JpaRepository<Squad, String> {
  // 특정 프로젝트 코드에 해당하는 스쿼드 개수 조회
  long countByProjectCode(String projectCode);

  Optional<Squad> findBySquadCode(String squadCode);

  List<Squad> findByProjectCode(String projectCode);

  Optional<Squad> findByProjectCodeAndIsActiveIsTrue(String projectCode);

  boolean existsByTitleAndProjectCode(String title, String projectCode);

  boolean existsByTitleAndProjectCodeAndSquadCodeNot(
      String title, String projectCode, String currentSquadCode);
}
