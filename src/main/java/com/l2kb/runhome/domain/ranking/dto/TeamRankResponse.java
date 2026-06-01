package com.l2kb.runhome.domain.ranking.dto;

public record TeamRankResponse(
        int rank,
        String teamName,
        int games,
        int wins,
        int losses,
        int draws,
        String winningPct,
        String gamesBehind,
        String recent
) {
}
