package com.nexus.sion.feature.project.command.domain.aggregate;

import com.nexus.sion.common.domain.BaseTimeEntity;
import jakarta.persistence.*;

import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "domain")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Domain extends BaseTimeEntity {
  // base entity : 생성일자, 수정일자 자동생성 및 업데이트 설정

  @Id
  @Column(name = "name", length = 30)
  private String name;

  // For testing
  public static Domain of(String domainName) {
    return Domain.builder()
        .name(domainName)
        .build();
  }
}
