package com.l2kb.runhome.domain.game.dto;

public record GameScoreResponse(
        String date,
        String gameTime,
        String stadium,
        String awayTeam,
        String homeTeam,
        String awayScore,
        String homeScore,
        boolean isLive,
        boolean isFinished,
        String gameState,
        int inning,
        String inningHalf,
        String cancelStatus,
        String winPitcher,
        String losePitcher,
        String savePitcher,
        WeatherInfo weather
) {
}
