package com.l2kb.runhome.domain.game.dto;

public record TeamComparisonResponse(
        String gameId,
        String gameDate,
        String gameTime,
        String stadium,
        String awayTeamName,
        String homeTeamName,
        String vsRecord,
        TeamStats awayStats,
        TeamStats homeStats
) {}
