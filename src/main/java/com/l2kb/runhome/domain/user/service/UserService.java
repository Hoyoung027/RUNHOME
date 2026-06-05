package com.l2kb.runhome.domain.user.service;

import com.l2kb.runhome.domain.team.entity.Team;
import com.l2kb.runhome.domain.team.repository.TeamRepository;
import com.l2kb.runhome.domain.user.dto.*;
import com.l2kb.runhome.domain.user.entity.User;
import com.l2kb.runhome.domain.user.repository.UserRepository;
import com.l2kb.runhome.global.exception.BusinessException;
import com.l2kb.runhome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
        }
        User user = User.of(request.nickname(), request.password());
        if (request.teamId() != null) {
            Team team = teamRepository.findById(request.teamId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
            user.updateFavoriteTeam(team);
        }
        return UserResponse.from(userRepository.save(user));
    }

    public UserLoginResponse login(UserLoginRequest request) {
        User user = userRepository.findByNickname(request.nickname())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (!user.matchesPassword(request.password())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
        return new UserLoginResponse(user.getId(), user.getNickname());
    }

    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserSummaryResponse::from)
                .toList();
    }

    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateFavoriteTeam(Long userId, UserUpdateTeamRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Team team = teamRepository.findById(request.teamId())
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
        user.updateFavoriteTeam(team);
        return UserResponse.from(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        userRepository.delete(user);
    }

    @Transactional
    public void deleteAllUsers() {
        userRepository.deleteAll();
    }

}
