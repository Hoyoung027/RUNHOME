package com.l2kb.runhome.domain.ranking.controller;

import com.l2kb.runhome.domain.ranking.dto.HitterTopResponse;
import com.l2kb.runhome.domain.ranking.dto.PitcherTopResponse;
import com.l2kb.runhome.domain.ranking.dto.TeamRankingResponse;
import com.l2kb.runhome.domain.ranking.service.RankingService;
import com.l2kb.runhome.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ranking", description = "KBO 순위 및 선수 기록 API")
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @Operation(summary = "팀 순위 조회", description = "현재 시즌 KBO 팀 순위를 반환합니다. (승, 패, 무, 승률, 게임차, 연속 포함)")
    @GetMapping
    public ResponseEntity<ApiResponse<TeamRankingResponse>> getTeamRankings() {
        return ResponseEntity.ok(ApiResponse.ok(rankingService.getTeamRankings()));
    }

    @Operation(summary = "타자 부문별 TOP 5", description = "타율, 홈런, 타점, 도루, 득점, 안타, 출루율, 장타율 각 부문 상위 5명을 반환합니다.")
    @GetMapping("/hitters")
    public ResponseEntity<ApiResponse<HitterTopResponse>> getHitterTop5() {
        return ResponseEntity.ok(ApiResponse.ok(rankingService.getHitterTop5()));
    }

    @Operation(summary = "투수 부문별 TOP 5", description = "평균자책점, 승리, 세이브, 승률, 홀드, 탈삼진 각 부문 상위 5명을 반환합니다.")
    @GetMapping("/pitchers")
    public ResponseEntity<ApiResponse<PitcherTopResponse>> getPitcherTop5() {
        return ResponseEntity.ok(ApiResponse.ok(rankingService.getPitcherTop5()));
    }
}
