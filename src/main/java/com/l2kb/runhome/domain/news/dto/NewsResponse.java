package com.l2kb.runhome.domain.news.dto;

public record NewsResponse(
        String title,
        String url,
        String category,
        String thumbnailUrl,
        String summary,
        String date
) {}
