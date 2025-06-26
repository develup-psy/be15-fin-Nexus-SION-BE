package com.nexus.sion.feature.project.command.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.project.command.domain.aggregate.Project;

public interface ProjectRepository extends JpaRepository<Project, String> {

  Optional<Project> findByProjectCode(String projectCode);
}
