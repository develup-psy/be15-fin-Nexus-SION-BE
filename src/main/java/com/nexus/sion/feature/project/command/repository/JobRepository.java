package com.nexus.sion.feature.project.command.repository;

import com.nexus.sion.feature.project.command.domain.aggregate.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, String> {
}
