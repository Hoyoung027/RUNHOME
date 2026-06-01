package com.l2kb.runhome.domain.game.dto;

public record TeamWarStats(
        String teamId,
        String teamName,
        String teamLogoUrl,
        String hitter12War,
        String hitter35War,
        String hitter69War
) {}
