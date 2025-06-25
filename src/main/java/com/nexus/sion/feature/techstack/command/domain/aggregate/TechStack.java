package com.nexus.sion.feature.techstack.command.domain.aggregate;

import jakarta.persistence.*;

import com.nexus.sion.common.domain.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tech_stack")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@ToString
public class TechStack extends BaseTimeEntity {
  // base entity : 생성일자, 수정일자 자동생성 및 업데이트 설정
  @Id
  @Column(name = "tech_stack_name")
  private String techStackName;

  // For testing
  public static TechStack of(String techStackName) {
    TechStack techStack = new TechStack();
    techStack.techStackName = techStackName;
    return techStack;
  }
}
