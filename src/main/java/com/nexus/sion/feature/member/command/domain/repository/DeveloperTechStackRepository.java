package com.nexus.sion.feature.member.command.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.DeveloperTechStack;

public interface DeveloperTechStackRepository extends JpaRepository<DeveloperTechStack, Long> {

  List<DeveloperTechStack> findAllByEmployeeIdentificationNumber(String employeeId);

  Optional<DeveloperTechStack> findByEmployeeIdentificationNumberAndTechStackName(
      String employeeId, String techStackName);
}
