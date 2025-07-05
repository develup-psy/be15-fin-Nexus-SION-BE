package com.nexus.sion.feature.project.command.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nexus.sion.feature.project.command.domain.aggregate.ProjectAndJob;
import com.nexus.sion.feature.squad.command.application.dto.internal.RequiredJobDto;

public interface ProjectAndJobRepository extends JpaRepository<ProjectAndJob, Long> {
  List<ProjectAndJob> findByProjectCode(String projectCode);

  void deleteByProjectCode(String projectCode);

  @Query(
      "SELECT pj.id AS projectAndJobId, pj.requiredNumber AS requiredNumber "
          + "FROM ProjectAndJob pj "
          + "WHERE pj.projectCode = :projectCode")
  List<RequiredJobDto> findRequiredJobsByProjectCode(@Param("projectCode") String projectCode);
}
