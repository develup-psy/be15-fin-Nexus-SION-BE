package com.nexus.sion.feature.project.command.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFpSummary;

public interface ProjectFpSummaryRepository extends JpaRepository<ProjectFpSummary, Long> {
  Optional<ProjectFpSummary> findByProjectCode(String projectCode);

  void deleteByProjectCode(String projectId);
}
