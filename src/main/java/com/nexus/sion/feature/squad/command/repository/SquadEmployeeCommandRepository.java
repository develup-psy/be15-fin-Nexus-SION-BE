package com.nexus.sion.feature.squad.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;

import java.util.List;

public interface SquadEmployeeCommandRepository extends JpaRepository<SquadEmployee, Long> {
  void deleteBySquadCode(String squadCode);

  List<SquadEmployee> findBySquadCode(String squadCode);
}
