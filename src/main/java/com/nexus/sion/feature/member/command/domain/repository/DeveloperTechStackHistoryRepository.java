package com.nexus.sion.feature.member.command.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.DeveloperTechStackHistory;

public interface DeveloperTechStackHistoryRepository
    extends JpaRepository<DeveloperTechStackHistory, Long> {}
