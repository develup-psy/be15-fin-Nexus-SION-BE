package com.nexus.sion.feature.project.command.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.project.command.domain.aggregate.ClientCompany;

@Repository
public interface ClientCompanyRepository extends JpaRepository<ClientCompany, String> {
  Optional<ClientCompany> findTopByClientCodeStartingWithOrderByClientCodeDesc(String codePrefix);

  boolean existsByCompanyName(String companyName);
}
