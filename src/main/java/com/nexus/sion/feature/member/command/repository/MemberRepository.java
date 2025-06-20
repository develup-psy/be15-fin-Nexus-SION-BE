package com.nexus.sion.feature.member.command.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

  boolean existsByEmail(String email);

  boolean existsByEmployeeIdentificationNumber(String employeeIdentificationNumber);

  Optional<Member> findByEmployeeIdentificationNumberAndDeletedAtIsNull(String employeeIdentificationNumber);
}
