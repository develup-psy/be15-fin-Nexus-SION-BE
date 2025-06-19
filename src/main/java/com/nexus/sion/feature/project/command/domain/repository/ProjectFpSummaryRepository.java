package com.nexus.sion.feature.project.command.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFpSummary;

public interface ProjectFpSummaryRepository extends JpaRepository<ProjectFpSummary, Long> {
}
