package com.nexus.sion.feature.member.command.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.feature.member.command.application.scheduler.MemberGradeScheduler;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/test/grade")
@RequiredArgsConstructor
public class MemberGradeTestController {

  private final MemberGradeScheduler memberGradeScheduler;

  @PostMapping("/update")
  public ResponseEntity<String> updateGradesManually() {
    memberGradeScheduler.updateDeveloperGrades();
    return ResponseEntity.ok("개발자 등급 산정 완료");
  }
}
