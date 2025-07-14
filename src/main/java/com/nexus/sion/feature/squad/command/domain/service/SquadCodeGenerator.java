package com.nexus.sion.feature.squad.command.domain.service;

public class SquadCodeGenerator {

  public static String generate(String projectCode, long squadCount) {
    return projectCode + "_" + (squadCount + 1);
  }
}
