package com.nexus.sion.feature.project.command.domain.aggregate;

import com.nexus.sion.common.domain.BaseTimeEntity;
import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "project_and_job")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ProjectAndJob  extends BaseTimeEntity {
  // base entity : 생성일자, 수정일자 자동생성 및 업데이트 설정

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "project_and_job_id")
  private Long id;

  @Column(name = "required_number", nullable = false)
  private int requiredNumber;

  @Column(name = "project_code", nullable = false, length = 30)
  private String projectCode;

  @Column(name = "job_name", nullable = false, length = 30)
  private String jobName;
}
