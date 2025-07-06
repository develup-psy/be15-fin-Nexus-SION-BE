package com.nexus.sion.feature.project.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.project.command.domain.aggregate.DeveloperProjectWork;

public interface DeveloperProjectWorkRepository extends JpaRepository<DeveloperProjectWork, Long> {}
