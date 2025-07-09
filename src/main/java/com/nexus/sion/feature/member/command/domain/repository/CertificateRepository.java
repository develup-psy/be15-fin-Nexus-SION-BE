package com.nexus.sion.feature.member.command.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Certificate;

public interface CertificateRepository extends JpaRepository<Certificate, String> {}
