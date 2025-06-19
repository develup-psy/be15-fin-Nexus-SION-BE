package com.nexus.sion.feature.member.command.repository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Member, String> {

    boolean existsByEmail(String email);

    boolean existsByEmployeeIdentificationNumber(String employeeIdentificationNumber);
}
