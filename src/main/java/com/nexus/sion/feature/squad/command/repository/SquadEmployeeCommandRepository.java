package com.nexus.sion.feature.squad.command.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;

public interface SquadEmployeeCommandRepository extends JpaRepository<SquadEmployee, Long> {
  void deleteBySquadCode(String squadCode);

  List<SquadEmployee> findBySquadCode(String squadCode);
}
