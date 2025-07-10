package com.nexus.sion.feature.project.command.domain.aggregate;

import jakarta.persistence.*;

import com.nexus.sion.common.domain.BaseTimeEntity;

import lombok.*;

@Entity
@Table(name = "developer_project_work_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeveloperProjectWorkHistory extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "developer_project_work_history_id")
  private Long id;

  @Column(name = "function_name", nullable = false, length = 100)
  private String functionName;

  @Column(name = "function_description", nullable = false, columnDefinition = "TEXT")
  private String functionDescription;

  @Enumerated(EnumType.STRING)
  @Column(name = "function_type", nullable = false)
  private FunctionType functionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "complexity")
  private Complexity complexity;

  @Column(name = "developer_project_work_id", nullable = false)
  private Long developerProjectWorkId;

  @Column(name = "det", nullable = false)
  private Integer det;

  @Column(name = "ftr", nullable = false)
  private Integer ftr;

  public enum FunctionType {
    EI,
    EO,
    EQ,
    ILF,
    EIF
  }

  public enum Complexity {
    SIMPLE,
    MEDIUM,
    COMPLEX
  }
}
