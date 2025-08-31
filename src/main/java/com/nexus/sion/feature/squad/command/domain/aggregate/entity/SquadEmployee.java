package com.nexus.sion.feature.squad.command.domain.aggregate.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import com.nexus.sion.common.domain.BaseTimeEntity;

import lombok.*;

@Entity
@Table(name = "squad_employee")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SquadEmployee extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "squad_employee_id")
  private Long id;

  @Column(name = "assigned_date")
  private LocalDate assignedDate;

  @Column(name = "employee_identification_number", nullable = false, length = 30)
  private String employeeIdentificationNumber;

  @Column(name = "project_and_job_id", nullable = false)
  private Long projectAndJobId;

  @Column(name = "is_leader", nullable = false)
  private boolean isLeader;

  @Column(name = "squad_code", nullable = false, length = 30)
  private String squadCode;
}
