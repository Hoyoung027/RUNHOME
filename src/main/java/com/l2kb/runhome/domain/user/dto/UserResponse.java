package com.l2kb.runhome.domain.user.dto;

import com.l2kb.runhome.domain.user.entity.User;

public record UserResponse(
        Long id,
        String nickname,
        String location,
        Long favoriteTeamId,
        String favoriteTeamName
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getNickname(),
                user.getLocation(),
                user.getFavoriteTeam() != null ? user.getFavoriteTeam().getId() : null,
                user.getFavoriteTeam() != null ? user.getFavoriteTeam().getName() : null
        );
    }
}
