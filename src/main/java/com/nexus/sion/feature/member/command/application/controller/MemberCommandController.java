package com.nexus.sion.feature.member.command.application.controller;

import com.nexus.sion.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
@Tag(name = "", description = "")
public class MemberCommandController {


    @PostMapping()
    public ResponseEntity<ApiResponse<Void>> register(@Re)

}
