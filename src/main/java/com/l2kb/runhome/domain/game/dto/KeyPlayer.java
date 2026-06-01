package com.l2kb.runhome.domain.game.dto;

public record KeyPlayer(
        String name,
        String teamLogoUrl,
        String playerImageUrl,
        String recentWinRate,
        String recentGameStats,
        String seasonBaVsOpponent,
        String seasonStatsVsOpponent
) {}
