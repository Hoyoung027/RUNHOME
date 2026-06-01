package com.l2kb.runhome.domain.user.dto;

import com.l2kb.runhome.domain.user.entity.User;

public record UserSummaryResponse(
        Long id,
        String nickname,
        Long favoriteTeamId,
        String favoriteTeamName
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(
                user.getId(),
                user.getNickname(),
                user.getFavoriteTeam() != null ? user.getFavoriteTeam().getId() : null,
                user.getFavoriteTeam() != null ? user.getFavoriteTeam().getName() : null
        );
    }
}
