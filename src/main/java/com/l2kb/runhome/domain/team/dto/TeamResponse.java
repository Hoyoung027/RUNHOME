package com.l2kb.runhome.domain.team.dto;

import com.l2kb.runhome.domain.team.entity.Team;

public record TeamResponse(
        Long id,
        String abbreviation,
        String name,
        String shortName,
        String homeCity,
        String stadiumName,
        String primaryColor,
        String secondaryColor,
        String logoUrl
) {
    public static TeamResponse from(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getAbbreviation(),
                team.getName(),
                team.getShortName(),
                team.getHomeCity(),
                team.getStadiumName(),
                team.getPrimaryColor(),
                team.getSecondaryColor(),
                team.getLogoUrl()
        );
    }
}
