package com.nexus.sion.feature.member.command.domain.repository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.DeveloperTechStack;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeveloperTechStackRepository extends JpaRepository<DeveloperTechStack, Long> {

    List<DeveloperTechStack> findAllByEmployeeIdentificationNumber(String employeeId);


}