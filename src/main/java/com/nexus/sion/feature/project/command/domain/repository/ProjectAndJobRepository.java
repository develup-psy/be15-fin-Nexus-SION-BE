package com.nexus.sion.feature.project.command.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.project.command.domain.aggregate.ProjectAndJob;

public interface ProjectAndJobRepository extends JpaRepository<ProjectAndJob, Long> {
  List<ProjectAndJob> findByProjectCode(String projectCode);

  void deleteByProjectCode(String projectCode);

}
