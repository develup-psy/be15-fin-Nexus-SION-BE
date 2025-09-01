package com.nexus.sion.feature.squad.command.domain.service;

import org.springframework.stereotype.Service;

@Service
public class SquadCodeGenerator {

  public static String generate(String projectCode, long squadCount) {
    return projectCode + "_" + (squadCount + 1);
  }
}
