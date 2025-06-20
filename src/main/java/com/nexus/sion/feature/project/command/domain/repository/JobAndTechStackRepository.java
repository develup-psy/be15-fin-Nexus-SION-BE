package com.nexus.sion.feature.project.command.domain.repository;

import com.nexus.sion.feature.project.command.domain.aggregate.JobAndTechStack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobAndTechStackRepository extends JpaRepository<JobAndTechStack, Long> {
}