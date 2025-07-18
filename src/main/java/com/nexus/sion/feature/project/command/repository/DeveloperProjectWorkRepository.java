package com.nexus.sion.feature.project.command.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWork;

public interface DeveloperProjectWorkRepository extends JpaRepository<DeveloperProjectWork, Long> {
  List<DeveloperProjectWork> findByProjectCode(String projectCode);

    List<DeveloperProjectWork> findAllByProjectCode(String projectCode);
}
