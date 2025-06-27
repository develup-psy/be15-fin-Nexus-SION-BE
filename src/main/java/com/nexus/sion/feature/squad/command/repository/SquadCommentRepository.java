package com.nexus.sion.feature.squad.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadComment;

public interface SquadCommentRepository extends JpaRepository<SquadComment, Long> {
    void deleteBySquadCode(String squadCode);
}
