package com.nexus.sion.feature.member.command.domain.repository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Position;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, String> {
}