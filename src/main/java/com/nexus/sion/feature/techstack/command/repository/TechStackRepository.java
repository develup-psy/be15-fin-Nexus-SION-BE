package com.nexus.sion.feature.techstack.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.techstack.command.domain.aggregate.TechStack;

public interface TechStackRepository extends JpaRepository<TechStack, String> {}
