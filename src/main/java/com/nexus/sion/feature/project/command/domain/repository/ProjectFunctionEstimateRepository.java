package com.nexus.sion.feature.project.command.domain.repository;

import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFunctionEstimate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectFunctionEstimateRepository extends JpaRepository<ProjectFunctionEstimate, Long> {
}
