package com.nexus.sion.feature.squad.command.repository;

import com.nexus.sion.feature.squad.command.domain.aggregate.entity.SquadComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SquadCommentRepository extends JpaRepository<SquadComment, Long> {
}
