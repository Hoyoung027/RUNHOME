package com.l2kb.runhome.domain.team.controller;

import com.l2kb.runhome.domain.team.dto.TeamResponse;
import com.l2kb.runhome.domain.team.service.TeamService;
import com.l2kb.runhome.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Team", description = "KBO 구단 관련 API")
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "전체 구단 목록 조회", description = "KBO 10개 구단의 정보를 모두 반환합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TeamResponse>>> getAllTeams() {
        return ResponseEntity.ok(ApiResponse.ok(teamService.getAllTeams()));
    }

    @Operation(summary = "구단 단건 조회", description = "구단 ID로 특정 구단 정보를 조회합니다.")
    @GetMapping("/{teamId}")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeam(@PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.ok(teamService.getTeam(teamId)));
    }
}
