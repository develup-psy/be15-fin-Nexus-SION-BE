package com.nexus.sion.feature.project.command.domain.repository;

import com.nexus.sion.feature.project.command.domain.aggregate.ProjectAndJob;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectAndJobRepository extends JpaRepository<ProjectAndJob, Long> {
}
