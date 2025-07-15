package com.nexus.sion.feature.project.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWorkHistoryTechStack;

import java.util.Collection;
import java.util.List;

public interface DeveloperProjectWorkHistoryTechStackRepository
    extends JpaRepository<DeveloperProjectWorkHistoryTechStack, Long> {
    List<DeveloperProjectWorkHistoryTechStack> findAllByDeveloperProjectWorkHistoryIdIn(List<Long> developerProjectWorkHistoryIds);
}
