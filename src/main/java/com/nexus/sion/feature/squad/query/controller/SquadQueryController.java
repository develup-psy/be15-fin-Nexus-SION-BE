package com.nexus.sion.feature.squad.query.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nexus.sion.feature.squad.query.dto.request.SquadListRequest;
import com.nexus.sion.feature.squad.query.dto.response.SquadListResponse;
import com.nexus.sion.feature.squad.query.service.SquadQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/squads")
@RequiredArgsConstructor
public class SquadQueryController {

    private final SquadQueryService squadQueryService;

    @GetMapping("/project/{projectCode}")
    public List<SquadListResponse> getSquads(@PathVariable String projectCode) {
        SquadListRequest request = new SquadListRequest();
        request.setProjectCode(projectCode);
        return squadQueryService.findSquads(request);
    }
}
