package com.nexus.sion.feature.project.command.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nexus.sion.feature.project.command.domain.aggregate.Project;

public interface ProjectRepository extends JpaRepository<Project, String> {

  Optional<Project> findByProjectCode(String projectCode);

  @Query(
      "SELECT p.projectCode FROM Project p WHERE p.clientCode = :clientCode AND p.projectCode LIKE CONCAT(:clientCode, '_%')")
  List<String> findProjectCodesByClientCode(String clientCode);
}
