package com.nexus.sion.feature.project.command.domain.aggregate.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_and_tech_stack")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAndTechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_and_tech_stack_id")
    private Long id;

    @Column(name = "tech_stack_id", length = 30, nullable = false)
    private String techStackId;

    /*@ManyToOne
    @JoinColumn(name = "project_and_job_id", nullable = false)
    private ProjectAndJob projectAndJob;*/

    @Column(name = "project_and_job_id", nullable = false)
    private String projectAndJob;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}