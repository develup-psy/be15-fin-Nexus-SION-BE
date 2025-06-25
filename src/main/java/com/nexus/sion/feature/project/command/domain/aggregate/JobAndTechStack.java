package com.nexus.sion.feature.project.command.domain.aggregate;

import jakarta.persistence.*;

import com.nexus.sion.common.domain.BaseTimeEntity;

import lombok.*;

@Entity
@Table(name = "job_and_tech_stack")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAndTechStack extends BaseTimeEntity {
  // base entity : 생성일자, 수정일자 자동생성 및 업데이트 설정

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "job_and_tech_stack_id")
  private Long id;

  @Column(name = "tech_stack_name", length = 30, nullable = false)
  private String techStackName;

  @Column(name = "project_and_job_id", nullable = false)
  private Long projectJobId;

  @Column(name = "priority")
  private Integer priority;
}
