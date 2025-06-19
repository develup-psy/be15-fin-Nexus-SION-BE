package com.nexus.sion.feature.project.command.domain.repository;

import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFpSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectFpSummaryRepository extends JpaRepository<ProjectFpSummary, Long> {
}
