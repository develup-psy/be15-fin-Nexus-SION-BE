package com.nexus.sion.feature.project.command.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.project.command.domain.aggregate.JobAndTechStack;

import java.util.List;

public interface JobAndTechStackRepository extends JpaRepository<JobAndTechStack, Long> {
  void deleteByProjectJobId(Long projectJobId);

  List<JobAndTechStack> findByTechStackName(String techStackName);

  boolean existsByTechStackName(String techStackName);
}
