package com.nexus.sion.feature.squad.query.dto.response;

import java.util.List;
import java.util.Map;

public record DeveloperSummary(
        String id ,
        String name,
        String grade,
        Map<String, Double> stackScores,
        List<String> domains
) {}
