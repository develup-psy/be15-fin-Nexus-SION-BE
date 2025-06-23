package com.nexus.sion.feature.member.command.domain.repository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, String> {
}