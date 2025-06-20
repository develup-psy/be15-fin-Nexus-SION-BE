package com.nexus.sion.feature.member.command.domain.aggregate.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "developer_tech_stack")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DeveloperTechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "developer_tech_stack_id")
    private Long id;

    @Column(name = "tech_stack_total_scores", nullable = false)
    private int totalScore;

    @Column(name = "employee_identification_number", nullable = false, length = 30)
    private String employeeIdentificationNumber;

    @Column(name = "tech_stack_name", nullable = false, length = 30)
    private String techStackName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
