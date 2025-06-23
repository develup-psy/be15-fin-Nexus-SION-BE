package com.nexus.sion.feature.member.command.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Position;

public interface PositionRepository extends JpaRepository<Position, String> {}
