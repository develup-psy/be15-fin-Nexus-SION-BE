package com.nexus.sion.feature.squad.command.application.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nexus.sion.feature.squad.command.application.dto.request.SquadRegisterRequest;
import com.nexus.sion.feature.squad.command.application.dto.request.SquadUpdateRequest;
import com.nexus.sion.feature.squad.command.application.service.SquadCommandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/squads")
@RequiredArgsConstructor
public class SquadCommandController {

  private final SquadCommandService squadCommandService;

  @PostMapping("/manual")
  public ResponseEntity<Void> registerManualSquad(@RequestBody @Valid SquadRegisterRequest request) {
    squadCommandService.registerManualSquad(request);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/manual")
  public ResponseEntity<Void> updateManualSquad(@RequestBody @Valid SquadUpdateRequest request) {
    squadCommandService.updateManualSquad(request);
    return ResponseEntity.ok().build();
  }

  @DeleteMapping("/{squadCode}")
  public ResponseEntity<Void> deleteSquad(@PathVariable String squadCode) {
    squadCommandService.deleteSquad(squadCode);
    return ResponseEntity.noContent().build();
  }

}
