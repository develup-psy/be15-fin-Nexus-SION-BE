package com.nexus.sion.feature.member.command.domain.repository;

import com.nexus.sion.feature.member.query.dto.response.CertificateResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Certificate;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface CertificateRepository extends JpaRepository<Certificate, String> {

    @Query("SELECT new com.nexus.sion.feature.member.query.dto.response.CertificateResponse(" +
            "c.certificateName, c.score, c.issuingOrganization) FROM Certificate c")
    List<CertificateResponse> findAllAsResponse();
}