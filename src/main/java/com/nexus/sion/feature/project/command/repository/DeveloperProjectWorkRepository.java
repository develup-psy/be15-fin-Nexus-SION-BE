package com.nexus.sion.feature.project.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWork;

import java.util.List;

public interface DeveloperProjectWorkRepository extends JpaRepository<DeveloperProjectWork, Long> {
    List<DeveloperProjectWork> findByProjectCode(String projectCode);
}
