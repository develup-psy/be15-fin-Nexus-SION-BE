package com.nexus.sion.feature.squad.query.controller;

import java.util.List;

import com.nexus.sion.common.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.feature.squad.query.dto.response.SquadCommentResponse;
import com.nexus.sion.feature.squad.query.service.SquadCommentQueryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/squads")
@RequiredArgsConstructor
public class SquadCommentQueryController {

  private final SquadCommentQueryService squadCommentQueryService;

  @GetMapping("/{squadCode}/comments")
  public ResponseEntity<ApiResponse<List<SquadCommentResponse>>> getComments(@PathVariable String squadCode) {
    List<SquadCommentResponse> comments =
        squadCommentQueryService.findCommentsBySquadCode(squadCode);
    return ResponseEntity.ok(ApiResponse.success(comments));
  }
}
