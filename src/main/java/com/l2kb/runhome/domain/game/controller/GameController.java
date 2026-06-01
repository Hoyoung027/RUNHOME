package com.l2kb.runhome.domain.game.controller;

import com.l2kb.runhome.domain.game.dto.GameScoreResponse;
import com.l2kb.runhome.domain.game.service.GameService;
import com.l2kb.runhome.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
}
