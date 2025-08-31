package com.nexus.sion.feature.project.query.dto.internal;

import java.math.BigDecimal;

public record ProjectEvaluationInfo (
    BigDecimal maxBudget,
    Integer maxDuration,
    int totalFP
){}
