package com.nexus.sion.feature.member.command.domain.aggregate.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.nexus.sion.feature.member.command.domain.aggregate.enums.GradeCode;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberRole;
import com.nexus.sion.feature.member.command.domain.aggregate.enums.MemberStatus;

import lombok.*;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
public class Member {

  @Id
  @Column(name = "employee_identification_number", length = 30)
  private String employeeIdentificationNumber;

  @Column(name = "employee_name", nullable = false, length = 30)
  private String employeeName;

  @Column(nullable = false, length = 255)
  private String password;

  @Column(name = "profile_image_url", length = 100)
  private String profileImageUrl;

  @Column(name = "phone_number", nullable = false, length = 11)
  private String phoneNumber;

  @Column(name = "position_name")
  private String positionName;

  @Column(name = "department_name")
  private String departmentName;

  @Column(name = "birthday")
  private LocalDate birthday;

  @Column(name = "joined_at")
  private LocalDateTime joinedAt;

  @Column(nullable = false, length = 30)
  private String email;

  @Builder.Default
  @Column(name = "career_years")
  private Integer careerYears = 1;

  private Long salary;

  @Enumerated(EnumType.STRING)
  @Column(name = "status")
  private MemberStatus status;

  @Enumerated(EnumType.STRING)
  @Column(name = "grade_code")
  private GradeCode gradeCode;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Setter
  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private MemberRole role;

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = this.createdAt;
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public void setEncodedPassword(String encodedPassword) {
    this.password = encodedPassword;
  }

  public void setAdminRole() {
    role = MemberRole.ADMIN;
  }

  public void update(
      String employeeName,
      String phoneNumber,
      LocalDate birthday,
      LocalDateTime joinedAt,
      String email,
      Integer careerYears,
      String positionName,
      String departmentName,
      String profileImageUrl,
      Long salary) {
    this.employeeName = employeeName;
    this.phoneNumber = phoneNumber;
    this.birthday = birthday;
    this.joinedAt = joinedAt;
    this.email = email;
    this.careerYears = careerYears;
    this.positionName = positionName;
    this.departmentName = departmentName;
    this.profileImageUrl = profileImageUrl;
    this.salary = salary;
  }
}
