package com.l2kb.runhome.domain.game.dto;

public record KeyPlayersResponse(
        String gameId,
        String gameDate,
        String gameTime,
        String stadium,
        String awayTeamName,
        String homeTeamName,
        KeyPlayer awayKeyPlayer,
        KeyPlayer homeKeyPlayer
) {}
