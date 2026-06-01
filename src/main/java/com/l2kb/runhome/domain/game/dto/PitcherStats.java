package com.l2kb.runhome.domain.game.dto;

public record PitcherStats(
        int pitcherId,
        String name,
        String pitchingStyle,
        String era,
        String war,
        int games,
        String avgInnings,
        int qualityStarts,
        String whip
) {}
