package com.l2kb.runhome.domain.game.controller;

import com.l2kb.runhome.domain.game.dto.*;
import com.l2kb.runhome.domain.game.service.GameService;
import com.l2kb.runhome.domain.game.service.VsAnalysisService;
import com.l2kb.runhome.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Game", description = "KBO 경기 일정 및 실시간 스코어 API")
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final VsAnalysisService vsAnalysisService;

    @Operation(
            summary = "경기 목록 조회",
            description = "지정한 날짜의 KBO 경기 목록과 스코어를 반환합니다. " +
                    "date 미입력 시 오늘 날짜 기준으로 조회합니다. " +
                    "X-User-Id 헤더 입력 시 해당 유저의 선호 팀 경기가 최상단에 반환됩니다. " +
                    "gameState: '경기 전' | '진행중' | '종료', isLive: 경기 진행 여부"
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<GameScoreResponse>>> getGameList(
            @Parameter(description = "조회할 날짜 (yyyyMMdd 형식, 예: 20260530). 미입력 시 오늘 날짜 적용")
            @RequestParam(required = false) String date,
            @Parameter(description = "요청 사용자 ID. 입력 시 선호 팀 경기가 최상단에 정렬됨")
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(gameService.getGameList(date, userId)));
    }

    @Operation(
            summary = "오늘 선발 투수 맞대결 분석",
            description = "오늘 경기의 선발 투수 시즌 성적을 반환합니다. " +
                    "X-User-Id 입력 시 선호 팀 경기만, 미입력 시 전체 경기를 반환합니다. " +
                    "선발이 미발표된 경기는 제외됩니다."
    )
    @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, description = "사용자 ID (선택)", required = false)
    @GetMapping("/pitcher-analysis")
    public ResponseEntity<ApiResponse<List<PitcherAnalysisResponse>>> getPitcherAnalysis(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(gameService.getPitcherAnalysis(userId)));
    }

    @Operation(summary = "팀 전력 비교", description = "다음 예정 경기의 맞대결 팀 ERA·타율·득실점 비교와 상대 전적을 반환합니다.")
    @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, description = "사용자 ID (선택)", required = false)
    @GetMapping("/team-comparison")
    public ResponseEntity<ApiResponse<List<TeamComparisonResponse>>> getTeamComparison(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(vsAnalysisService.getTeamComparison(userId)));
    }

    @Operation(summary = "키플레이어 비교", description = "다음 예정 경기의 팀별 키플레이어 최근 성적 및 상대 팀 상대 타율을 반환합니다.")
    @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, description = "사용자 ID (선택)", required = false)
    @GetMapping("/key-players")
    public ResponseEntity<ApiResponse<List<KeyPlayersResponse>>> getKeyPlayers(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(vsAnalysisService.getKeyPlayers(userId)));
    }

    @Operation(summary = "라인업 WAR 분석", description = "다음 예정 경기의 팀별 타순 구간(1-2번/3-5번/6-9번)별 WAR을 반환합니다.")
    @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, description = "사용자 ID (선택)", required = false)
    @GetMapping("/lineup-analysis")
    public ResponseEntity<ApiResponse<List<LineupWarResponse>>> getLineupAnalysis(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(vsAnalysisService.getLineupAnalysis(userId)));
    }
}
