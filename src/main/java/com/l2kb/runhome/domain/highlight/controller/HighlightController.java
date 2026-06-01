package com.l2kb.runhome.domain.highlight.controller;

import com.l2kb.runhome.domain.highlight.dto.HighlightResponse;
import com.l2kb.runhome.domain.highlight.service.HighlightService;
import com.l2kb.runhome.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Highlight", description = "KBO 하이라이트 API")
@RestController
@RequestMapping("/api/highlights")
@RequiredArgsConstructor
public class HighlightController {

    private final HighlightService highlightService;

    @Operation(
            summary = "하이라이트 조회",
            description = "최신 KBO 하이라이트 목록을 반환합니다. X-User-Id 헤더가 있으면 선호 구단 경기만, 없으면 전체 경기를 반환합니다."
    )
    @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, description = "사용자 ID (선택)", required = false)
    @GetMapping
    public ResponseEntity<ApiResponse<List<HighlightResponse>>> getHighlights(
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(highlightService.getHighlights(userId)));
    }
}
