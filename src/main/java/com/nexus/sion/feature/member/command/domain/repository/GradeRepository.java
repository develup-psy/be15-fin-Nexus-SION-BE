package com.nexus.sion.feature.member.command.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Grade;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;

@Repository
public interface GradeRepository extends JpaRepository<Grade, GradeCode> {
  Optional<Grade> findByGradeCode(GradeCode gradeCode);

  List<Grade> findAllByOrderByScoreThresholdDesc();
}
