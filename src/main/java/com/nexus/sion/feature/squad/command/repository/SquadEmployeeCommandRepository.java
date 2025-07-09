package com.nexus.sion.feature.squad.command.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;

public interface SquadEmployeeCommandRepository extends JpaRepository<SquadEmployee, Long> {

  void deleteBySquadCode(String squadCode);

  @Query("SELECT s FROM SquadEmployee s WHERE s.projectAndJobId = :projectAndJobId")
  List<SquadEmployee> findByProjectAndJobId(@Param("projectAndJobId") Long projectAndJobId);
}
