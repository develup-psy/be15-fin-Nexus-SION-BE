package com.nexus.sion.feature.techstack.command.repository;

import com.nexus.sion.feature.techstack.command.domain.aggregate.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechStackRepository extends JpaRepository<TechStack, String> {
}
