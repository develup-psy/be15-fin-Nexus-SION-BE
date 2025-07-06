package com.nexus.sion.feature.member.command.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Freelancer;

public interface FreelancerRepository extends JpaRepository<Freelancer, String> {
  // freelancer_id가 String(VARCHAR(30)) 이므로 ID 타입은 String
}
