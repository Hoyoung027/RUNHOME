package com.l2kb.runhome.domain.game.dto;

public record PitcherAnalysisResponse(
        String gameId,
        String gameDate,
        String gameTime,
        String stadium,
        String awayTeamName,
        String homeTeamName,
        PitcherStats awayPitcher,
        PitcherStats homePitcher
) {}
