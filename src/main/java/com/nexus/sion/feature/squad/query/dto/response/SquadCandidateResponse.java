package com.nexus.sion.feature.squad.query.dto.response;

import java.util.List;
import java.util.Map;

public record SquadCandidateResponse(
        Map<String, List<DeveloperSummary>> candidates
) {}
