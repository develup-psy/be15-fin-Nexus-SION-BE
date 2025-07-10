package com.nexus.sion.feature.member.command.domain.repository;

import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.nexus.sion.feature.member.command.domain.aggregate.entity.Member;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

  boolean existsByEmail(String email);

  boolean existsByEmployeeIdentificationNumber(String employeeIdentificationNumber);

  Optional<Member> findByEmployeeIdentificationNumberAndDeletedAtIsNull(
      String employeeIdentificationNumber);

  @Query(
      "SELECT m.employeeName FROM Member m WHERE m.employeeIdentificationNumber = :employeeIdentificationNumber")
  Optional<String> findEmployeeNameByEmployeeIdentificationNumber(
      @Param("employeeIdentificationNumber") String employeeIdentificationNumber);

  boolean existsByEmployeeIdentificationNumberAndRole(String adminId, MemberRole memberRole);
}
