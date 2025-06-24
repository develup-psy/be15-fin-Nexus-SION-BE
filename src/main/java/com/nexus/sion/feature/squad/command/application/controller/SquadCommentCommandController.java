package com.nexus.sion.feature.squad.command.application.controller;

import com.nexus.sion.feature.squad.command.application.dto.request.SquadCommentRegisterRequest;
import com.nexus.sion.feature.squad.command.application.service.SquadCommentCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/squads")
@RequiredArgsConstructor
public class SquadCommentCommandController {

    private final SquadCommentCommandService squadCommentCommandService;

    @PostMapping("/{squadCode}/comments")
    public ResponseEntity<Void> register(
            @PathVariable String squadCode,
            @RequestBody @Valid SquadCommentRegisterRequest request
    ) {
        squadCommentCommandService.registerComment(squadCode, request); // 👈 squadCode 따로 전달
        return ResponseEntity.ok().build();
    }

}
