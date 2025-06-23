package com.nexus.sion.feature.techstack.command.domain.aggregate;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tech_stack")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class TechStack {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "tech_stack_name")
  private Long techStackName;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
