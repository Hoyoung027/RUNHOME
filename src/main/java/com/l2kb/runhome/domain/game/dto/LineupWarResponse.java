package com.l2kb.runhome.domain.game.dto;

public record LineupWarResponse(
        String gameId,
        String gameDate,
        String gameTime,
        String stadium,
        String awayTeamName,
        String homeTeamName,
        boolean lineupConfirmed,
        TeamWarStats awayWar,
        TeamWarStats homeWar
) {}
