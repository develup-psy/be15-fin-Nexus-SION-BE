package com.nexus.sion.feature.member.command.repository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Grade;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GradeCommandRepository extends JpaRepository<Grade, GradeCode> {
    boolean existsByGradeCodeIsNotNull();

    Optional<Grade> findByGradeCode(GradeCode gradeCode);
}
