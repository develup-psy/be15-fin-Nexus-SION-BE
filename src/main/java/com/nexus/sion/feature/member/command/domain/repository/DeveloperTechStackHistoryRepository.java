package com.nexus.sion.feature.member.command.domain.repository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.DeveloperTechStack;
import com.nexus.sion.feature.member.command.domain.aggregate.entity.DeveloperTechStackHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeveloperTechStackHistoryRepository extends JpaRepository<DeveloperTechStackHistory, Long> {

}
