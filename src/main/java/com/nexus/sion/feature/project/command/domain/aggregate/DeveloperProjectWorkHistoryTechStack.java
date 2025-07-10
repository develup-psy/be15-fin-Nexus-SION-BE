package com.nexus.sion.feature.project.command.domain.aggregate;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "developer_project_work_history_tech_stack")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class DeveloperProjectWorkHistoryTechStack {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "developer_project_work_history_tech_stack_id")
  private Long id;

  @Column(name = "developer_project_work_history_id", nullable = false)
  private Long developerProjectWorkHistoryId;

  @Column(name = "tech_stack_name", nullable = false)
  private String techStackName;
}
