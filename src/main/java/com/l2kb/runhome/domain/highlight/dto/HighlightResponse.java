package com.l2kb.runhome.domain.highlight.dto;

public record HighlightResponse(
        String title,
        String thumbnailUrl,
        String youtubeUrl,
        String gameDate,
        Long awayTeamId,
        String awayTeamShortName,
        Long homeTeamId,
        String homeTeamShortName,
        String stadiumName
) {}
