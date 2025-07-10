package com.nexus.sion.feature.squad.command.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadEmployee;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SquadEmployeeCommandRepository extends JpaRepository<SquadEmployee, Long> {
  void deleteBySquadCode(String squadCode);

  List<SquadEmployee> findBySquadCode(String squadCode);

  @Query("""
    SELECT se
    FROM SquadEmployee se
    JOIN ProjectAndJob pj ON se.projectAndJobId = pj.id
    WHERE pj.projectCode = :projectCode
""")
  List<SquadEmployee> findByProjectCode(@Param("projectCode") String projectCode);
}
