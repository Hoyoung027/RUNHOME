package com.l2kb.runhome.domain.user.dto;

public record UserCreateRequest(
        String nickname,
        String password,
        Long teamId
) {
}
