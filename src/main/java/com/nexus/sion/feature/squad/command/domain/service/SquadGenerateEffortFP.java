package com.nexus.sion.feature.squad.command.domain.service;

import org.springframework.stereotype.Service;

@Service
public class SquadGenerateEffortFP {
  public static double getEffortRatePerFP(int totalFp) {
    if (totalFp <= 100) return 0.15;
    else if (totalFp <= 300) return 0.125;
    else if (totalFp <= 600) return 0.10;
    else return 0.08;
  }
}
