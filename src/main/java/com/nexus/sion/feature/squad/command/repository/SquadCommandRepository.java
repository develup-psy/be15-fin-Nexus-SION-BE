package com.nexus.sion.feature.squad.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.squad.command.domain.aggregate.entity.Squad;

public interface SquadCommandRepository extends JpaRepository<Squad, String> {

  // 특정 프로젝트 코드에 해당하는 스쿼드 개수 조회
  long countByProjectCode(String projectCode);
}
