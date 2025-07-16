package com.nexus.sion.feature.project.command.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWorkHistoryTechStack;

public interface DeveloperProjectWorkHistoryTechStackRepository
    extends JpaRepository<DeveloperProjectWorkHistoryTechStack, Long> {
  List<DeveloperProjectWorkHistoryTechStack> findAllByDeveloperProjectWorkHistoryIdIn(
      List<Long> developerProjectWorkHistoryIds);
}
