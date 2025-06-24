package com.nexus.sion.feature.project.command.repository;

import com.nexus.sion.feature.project.command.domain.aggregate.Domain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainRepository extends JpaRepository<Domain, String> {
}
