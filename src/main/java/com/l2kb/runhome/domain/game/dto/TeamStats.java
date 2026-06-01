package com.l2kb.runhome.domain.game.dto;

public record TeamStats(
        String teamName,
        String era,
        boolean eraWin,
        String battingAverage,
        boolean battingAverageWin,
        String runsScored,
        boolean runsScoredWin,
        String runsAllowed
) {}
