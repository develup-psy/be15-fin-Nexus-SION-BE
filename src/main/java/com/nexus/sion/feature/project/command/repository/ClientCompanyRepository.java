package com.nexus.sion.feature.project.command.repository;

import com.nexus.sion.feature.project.command.domain.aggregate.ClientCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientCompanyRepository extends JpaRepository<ClientCompany, String> {
        Optional<ClientCompany> findTopByClientCodeStartingWithOrderByClientCodeDesc(String codePrefix);

        boolean existsByCompanyName(String companyName);
}
