package com.nexus.sion.feature.squad.command.domain.service;


public class SquadCodeGenerator {

    public static String generate(String projectCode, long squadCount) {
        // 예: ha_1_1 + _2 → ha_1_1_2
        return projectCode + "_" + (squadCount + 1);
    }
}

