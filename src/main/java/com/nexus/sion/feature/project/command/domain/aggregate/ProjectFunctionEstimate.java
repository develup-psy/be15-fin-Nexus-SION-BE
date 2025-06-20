package com.nexus.sion.feature.project.command.domain.aggregate;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "project_function_estimate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "project_fp_summary_id", nullable = false, length = 255)
    private String projectFpSummaryId;

    public enum FunctionType {
        EI, EO, EQ, ILF, EIF
    }

    public enum Complexity {
        SIMPLE, MEDIUM, COMPLEX
    }
}
