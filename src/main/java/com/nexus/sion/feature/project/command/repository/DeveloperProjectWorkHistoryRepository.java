package com.nexus.sion.feature.project.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWorkHistory;

import java.util.List;

public interface DeveloperProjectWorkHistoryRepository
    extends JpaRepository<DeveloperProjectWorkHistory, Long> {
    List<DeveloperProjectWorkHistory> findAllByDeveloperProjectWorkId(Long id);
}
