package com.nexus.sion.feature.project.command.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.project.command.domain.aggregate.JobAndTechStack;

public interface JobAndTechStackRepository extends JpaRepository<JobAndTechStack, Long> {
  void deleteByProjectJobId(Long projectJobId);

  List<JobAndTechStack> findByTechStackName(String techStackName);

  boolean existsByTechStackName(String techStackName);
}
