package com.nexus.sion.feature.project.command.domain.aggregate;

import jakarta.persistence.*;

import com.nexus.sion.common.domain.BaseTimeEntity;
import com.nexus.sion.feature.project.command.application.dto.request.WorkHistoryItemDto;

import lombok.*;

@Entity
@Table(name = "developer_project_work_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeveloperProjectWorkHistory extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "developer_project_work_history_id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "developer_project_work_id", nullable = false)
  private DeveloperProjectWork developerProjectWork;

  @Column(name = "function_description", nullable = false, columnDefinition = "TEXT")
  private String functionDescription;

  @Column(name = "tech_stack_name", nullable = false)
  private String techStackName;

  @Enumerated(EnumType.STRING)
  @Column(name = "function_type", nullable = false)
  private FunctionType functionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "complexity", nullable = false)
  private Complexity complexity;

  public DeveloperProjectWorkHistory(
      String functionDescription,
      String techStackName,
      FunctionType functionType,
      Complexity complexity) {
    this.functionDescription = functionDescription;
    this.techStackName = techStackName;
    this.functionType = functionType;
    this.complexity = complexity;
  }

  public DeveloperProjectWorkHistory(DeveloperProjectWork work, WorkHistoryItemDto dto) {
    this.developerProjectWork = work;
    this.functionDescription = dto.getFunctionDescription();
    this.techStackName = dto.getTechStackName();
    this.functionType = FunctionType.valueOf(dto.getFunctionType());
    this.complexity = Complexity.valueOf(dto.getComplexity());
  }

  public void setDeveloperProjectWork(DeveloperProjectWork work) {
    this.developerProjectWork = work;
  }

  public enum FunctionType {
    EI,
    EO,
    EQ,
    ILF,
    EIF
    // 기능 유형: EI(입력), EO(출력), EQ(조회), ILF(내부파일), EIF(외부파일)
  }

  public enum Complexity {
    SIMPLE,
    MEDIUM,
    COMPLEX
  }
}
