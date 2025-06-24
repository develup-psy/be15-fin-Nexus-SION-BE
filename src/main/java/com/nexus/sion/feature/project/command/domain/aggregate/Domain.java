package com.nexus.sion.feature.project.command.domain.aggregate;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "domain")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Domain {

  @Id
  @Column(name = "name", length = 30)
  private String name;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  // For testing
  public static Domain of(String domainName) {
    return Domain.builder()
            .name(domainName)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
  }
}
