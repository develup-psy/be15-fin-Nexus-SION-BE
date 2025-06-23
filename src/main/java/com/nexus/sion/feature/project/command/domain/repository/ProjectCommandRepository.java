package com.nexus.sion.feature.project.command.domain.repository;

import com.nexus.sion.feature.project.command.domain.aggregate.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectCommandRepository extends JpaRepository<Project, String> {
    boolean existsByProjectCode(String projectCode);
}
