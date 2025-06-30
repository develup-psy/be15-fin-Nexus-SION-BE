package com.nexus.sion.feature.project.command.domain.aggregate;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedDate;

import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "project_function_estimate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
@ToString
public class ProjectFunctionEstimate {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "project_function_estimate_id")
  private Long id;

  @Column(name = "function_name", nullable = false, length = 100)
  private String functionName;

  @Enumerated(EnumType.STRING)
  @Column(name = "function_type", nullable = false, length = 10)
  private FunctionType functionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "complexity", nullable = false, length = 10)
  private Complexity complexity;

  @Column(name = "function_score", nullable = false)
  private int functionScore;

  @Lob
  @Column(name = "description")
  private String description;

  @Column(name = "related_tables_count", nullable = false)
  private int relatedTablesCount;

  @Column(name = "related_fields_count")
  private Integer relatedFieldsCount;

  @CreatedDate
  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "project_fp_summary_id")
  private Long projectFpSummaryId;

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


  public static FunctionType fromString(String value) {
    try {
      return FunctionType.valueOf(value);
    } catch (IllegalArgumentException e) {
      return null; // 또는 Optional.empty(), null, UNKNOWN 등 처리
    }
  }
}
