package com.nexus.sion.feature.member.command.domain.aggregate.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "freelancer")
@Getter
@NoArgsConstructor
public class Freelancer {

  @Id
  @Column(name = "freelancer_id", nullable = false, length = 30)
  private String freelancerId;

  @Column(name = "name", nullable = false, length = 30)
  private String name;

  @Column(name = "phone_number", nullable = false, length = 20)
  private String phoneNumber;

  @Column(name = "email", nullable = false, length = 50)
  private String email;

  @Column(name = "career_years")
  private Integer careerYears;

  @Column(name = "resume_url", nullable = false, length = 255)
  private String resumeUrl;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "profile_image_url", length = 255)
  private String profileImageUrl;

  @Column(name = "birthday")
  private LocalDate birthday;
}
