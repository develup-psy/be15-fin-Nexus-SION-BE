package com.nexus.sion.feature.member.command.application.service;

import com.nexus.sion.feature.member.command.application.dto.request.UnitPriceSetRequest;

public interface GradeCommandService {
  void setGrades(UnitPriceSetRequest request);
}
