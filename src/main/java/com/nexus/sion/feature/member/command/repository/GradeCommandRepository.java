package com.nexus.sion.feature.member.command.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Grade;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;

public interface GradeCommandRepository extends JpaRepository<Grade, GradeCode> {
  Optional<Grade> findByGradeCode(GradeCode gradeCode);
}
