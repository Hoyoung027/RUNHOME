package com.l2kb.runhome.domain.news.controller;

import com.l2kb.runhome.domain.news.dto.NewsResponse;
import com.l2kb.runhome.domain.news.service.NewsService;
import com.l2kb.runhome.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "News", description = "KBO 뉴스 API")
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {

    private final NewsService newsService;

    @Operation(
            summary = "KBO 뉴스 목록 조회",
            description = "KBO 속보·인터뷰·사진 뉴스를 카테고리별로 스크래핑해 반환합니다. " +
                    "X-User-Id 입력 시 선호 팀 관련 뉴스만 필터링됩니다."
    )
    @Parameter(name = "X-User-Id", in = ParameterIn.HEADER, description = "사용자 ID (선택)", required = false)
    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsResponse>>> getNews(
            @Parameter(description = "페이지 번호 (기본값: 1)")
            @RequestParam(defaultValue = "1") int pageNo,
            @Parameter(description = "반환할 뉴스 수 (기본값: 10)")
            @RequestParam(defaultValue = "10") int listCn,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ResponseEntity.ok(ApiResponse.ok(newsService.getNews(pageNo, listCn, userId)));
    }
}
