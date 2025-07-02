package com.nexus.sion.feature.project.command.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.project.command.domain.aggregate.ProjectFpSummary;

import java.nio.channels.FileChannel;
import java.util.Optional;

public interface ProjectFpSummaryRepository extends JpaRepository<ProjectFpSummary, Long> {
    Optional<ProjectFpSummary> findByProjectCode(String projectCode);
}
