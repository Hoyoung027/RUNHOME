package com.l2kb.runhome.domain.team.service;

import com.l2kb.runhome.domain.team.dto.TeamResponse;
import com.l2kb.runhome.domain.team.repository.TeamRepository;
import com.l2kb.runhome.global.exception.BusinessException;
import com.l2kb.runhome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamService {

    private final TeamRepository teamRepository;

    public List<TeamResponse> getAllTeams() {
        return teamRepository.findAll().stream()
                .map(TeamResponse::from)
                .toList();
    }

    public TeamResponse getTeam(Long teamId) {
        return teamRepository.findById(teamId)
                .map(TeamResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.TEAM_NOT_FOUND));
    }
}
