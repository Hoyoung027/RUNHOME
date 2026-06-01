package com.l2kb.runhome.domain.team;

import com.l2kb.runhome.domain.team.entity.Team;
import com.l2kb.runhome.domain.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TeamDataInitializer implements ApplicationRunner {

    private final TeamRepository teamRepository;

    private static final List<Team> TEAM_DATA = List.of(
            Team.of("KIA",    "KIA 타이거즈",   "기아",   "광주", "광주-기아 챔피언스 필드",  "#EA0029", "#000000"),
            Team.of("SAMSUNG","삼성 라이온즈",   "삼성",   "대구", "대구 삼성 라이온즈 파크",  "#074CA1", "#C0C0C0"),
            Team.of("LG",     "LG 트윈스",      "LG",    "서울", "잠실야구장",              "#C30452", "#000000"),
            Team.of("DOOSAN", "두산 베어스",     "두산",   "서울", "잠실야구장",              "#131230", "#C0C0C0"),
            Team.of("KT",     "KT 위즈",        "KT",    "수원", "수원 KT 위즈 파크",       "#000000", "#E31837"),
            Team.of("SSG",    "SSG 랜더스",     "SSG",   "인천", "SSG 랜더스 필드",         "#CE0E2D", "#041E42"),
            Team.of("LOTTE",  "롯데 자이언츠",   "롯데",   "부산", "사직야구장",              "#002854", "#D00F31"),
            Team.of("HANWHA", "한화 이글스",     "한화",   "대전", "한화생명이글스 파크",      "#FF6600", "#000000"),
            Team.of("NC",     "NC 다이노스",     "NC",    "창원", "창원 NC 파크",            "#315288", "#C0A86E"),
            Team.of("KIWOOM", "키움 히어로즈",   "키움",   "서울", "고척 스카이돔",            "#820024", "#FFFFFF")
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (Team data : TEAM_DATA) {
            teamRepository.findByAbbreviation(data.getAbbreviation())
                    .ifPresentOrElse(
                            existing -> existing.update(
                                    data.getName(), data.getShortName(), data.getHomeCity(),
                                    data.getStadiumName(), data.getPrimaryColor(), data.getSecondaryColor()
                            ),
                            () -> teamRepository.save(data)
                    );
        }
    }
}
