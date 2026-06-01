package com.l2kb.runhome.domain.game.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.l2kb.runhome.domain.game.dto.GameScoreResponse;
import com.l2kb.runhome.domain.game.dto.PitcherAnalysisResponse;
import com.l2kb.runhome.domain.game.dto.PitcherStats;
import com.l2kb.runhome.domain.game.dto.WeatherInfo;
import com.l2kb.runhome.domain.team.entity.Team;
import com.l2kb.runhome.domain.user.entity.User;
import com.l2kb.runhome.domain.user.repository.UserRepository;
import com.l2kb.runhome.global.exception.BusinessException;
import com.l2kb.runhome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GameService {

    private final RestClient restClient;
    private final UserRepository userRepository;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String KBO_GAME_URL = "https://m.koreabaseball.com/ws/Kbo.asmx/GetKboGameList";
    private static final String KBO_WEATHER_URL = "https://m.koreabaseball.com/ws/Weather.asmx/GetNowWeather";
    private static final String KBO_PITCHER_ANALYSIS_URL = "https://www.koreabaseball.com/ws/Main.asmx/GetPitcherAnalysisRecord";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final Map<String, String> DB_TO_KBO = Map.of(
            "KIA", "HT", "LG", "LG", "DOOSAN", "OB", "SAMSUNG", "SS", "KT", "KT",
            "SSG", "SK", "LOTTE", "LT", "HANWHA", "HH", "NC", "NC", "KIWOOM", "WO"
    );

    public List<GameScoreResponse> getGameList(String date, Long userId) {
        List<JsonNode> rawGames;

        if (date != null && !date.isBlank()) {
            rawGames = fetchRawGameList(date);
        } else {
            rawGames = findNextScheduledRawGames();
        }

        Map<String, WeatherInfo> weatherCache = new HashMap<>();
        List<GameScoreResponse> games = new ArrayList<>();
        for (JsonNode game : rawGames) {
            String stadiumId = game.path("S_ID").asText();
            WeatherInfo weather = weatherCache.computeIfAbsent(stadiumId, this::fetchWeather);
            games.add(toGameScoreResponse(game, weather));
        }

        sortByFavoriteTeam(games, userId);
        return games;
    }

    private List<JsonNode> findNextScheduledRawGames() {
        for (int i = 0; i < 7; i++) {
            String date = LocalDate.now().plusDays(i).format(DATE_FORMAT);
            List<JsonNode> games = fetchRawGameList(date);
            boolean hasUpcoming = games.stream()
                    .anyMatch(g -> "1".equals(g.path("GAME_STATE_SC").asText()));
            if (hasUpcoming) return games;
        }
        return List.of();
    }

    public List<PitcherAnalysisResponse> getPitcherAnalysis(Long userId) {
        String kboTeamId = resolveKboTeamId(userId);
        List<JsonNode> upcomingGames = findNextScheduledGames(kboTeamId);

        return upcomingGames.stream()
                .map(this::toPitcherAnalysis)
                .toList();
    }

    private List<JsonNode> findNextScheduledGames(String kboTeamId) {
        for (int i = 0; i < 7; i++) {
            String date = LocalDate.now().plusDays(i).format(DATE_FORMAT);
            List<JsonNode> games = fetchRawGameList(date);

            List<JsonNode> upcoming = games.stream()
                    .filter(g -> "1".equals(g.path("GAME_STATE_SC").asText()))
                    .collect(Collectors.toList());

            if (upcoming.isEmpty()) continue;

            if (kboTeamId != null) {
                String teamId = kboTeamId;
                upcoming = upcoming.stream()
                        .filter(g -> teamId.equals(g.path("AWAY_ID").asText())
                                  || teamId.equals(g.path("HOME_ID").asText()))
                        .collect(Collectors.toList());
            }

            if (!upcoming.isEmpty()) return upcoming;
        }
        return List.of();
    }

    private List<JsonNode> fetchRawGameList(String date) {
        String body = "leId=1&srId=0&date=" + date;
        try {
            String response = restClient.post()
                    .uri(KBO_GAME_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Mobile Safari/537.36")
                    .header("Referer", "https://m.koreabaseball.com/")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = MAPPER.readTree(response);
            return StreamSupport.stream(root.path("game").spliterator(), false)
                    .collect(Collectors.toList());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private String resolveKboTeamId(Long userId) {
        if (userId == null) return null;
        User user = userRepository.findWithTeamById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getFavoriteTeam() == null) return null;
        return DB_TO_KBO.get(user.getFavoriteTeam().getAbbreviation());
    }

    private PitcherAnalysisResponse toPitcherAnalysis(JsonNode game) {
        String gameId = game.path("G_ID").asText();
        String awayId = game.path("AWAY_ID").asText();
        String homeId = game.path("HOME_ID").asText();

        boolean pitcherConfirmed = !game.path("T_PIT_P_ID").isNull();

        if (!pitcherConfirmed) {
            return new PitcherAnalysisResponse(
                    gameId,
                    game.path("G_DT_TXT").asText(),
                    game.path("G_TM").asText(),
                    game.path("S_NM").asText(),
                    game.path("AWAY_NM").asText(),
                    game.path("HOME_NM").asText(),
                    null,
                    null
            );
        }

        int awayPitId = game.path("T_PIT_P_ID").asInt();
        int homePitId = game.path("B_PIT_P_ID").asInt();

        String body = "leId=1&srId=0&seasonId=" + LocalDate.now().getYear()
                + "&awayTeamId=" + awayId
                + "&awayPitId=" + awayPitId
                + "&homeTeamId=" + homeId
                + "&homePitId=" + homePitId
                + "&groupsc=SEASON"
                + "&gameId=" + gameId;

        try {
            String response = restClient.post()
                    .uri(KBO_PITCHER_ANALYSIS_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36")
                    .header("Referer", "https://www.koreabaseball.com/")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = MAPPER.readTree(response);
            return new PitcherAnalysisResponse(
                    gameId,
                    game.path("G_DT_TXT").asText(),
                    game.path("G_TM").asText(),
                    game.path("S_NM").asText(),
                    game.path("AWAY_NM").asText(),
                    game.path("HOME_NM").asText(),
                    toPitcherStats(root.path("season_T")),
                    toPitcherStats(root.path("season_B"))
            );
        } catch (Exception e) {
            log.warn("투수 분석 조회 실패: gameId={}", gameId);
            return new PitcherAnalysisResponse(
                    gameId,
                    game.path("G_DT_TXT").asText(),
                    game.path("G_TM").asText(),
                    game.path("S_NM").asText(),
                    game.path("AWAY_NM").asText(),
                    game.path("HOME_NM").asText(),
                    null,
                    null
            );
        }
    }

    private PitcherStats toPitcherStats(JsonNode node) {
        return new PitcherStats(
                node.path("P_ID").asInt(),
                node.path("P_NM").asText().trim(),
                node.path("P_TYPE").asText(),
                node.path("ERA_RT").asText(),
                node.path("WAR_RT").asText(),
                node.path("GAME_CN").asInt(),
                node.path("START_AVG_INN2_CN").asText(),
                node.path("QS_CN").asInt(),
                node.path("WHIP_RT").asText()
        );
    }

    private WeatherInfo fetchWeather(String stadiumId) {
        try {
            String body = "season=" + LocalDate.now().getYear() + "&s_id=" + stadiumId;
            String response = restClient.post()
                    .uri(KBO_WEATHER_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Mobile Safari/537.36")
                    .header("Referer", "https://m.koreabaseball.com/")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = MAPPER.readTree(response);
            JsonNode item = root.path("list").get(0);
            if (item == null) return null;

            return new WeatherInfo(
                    item.path("icon_nm").asText(),
                    item.path("temp_va").asDouble(),
                    item.path("humi_va").asText(),
                    item.path("wdir_nm").asText(),
                    item.path("wspeed_va").asDouble(),
                    "https:" + item.path("icon_lk").asText()
            );
        } catch (Exception e) {
            log.warn("날씨 조회 실패: stadiumId={}", stadiumId);
            return null;
        }
    }

    private void sortByFavoriteTeam(List<GameScoreResponse> games, Long userId) {
        if (userId == null) return;

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return;

        Team favoriteTeam = userOpt.get().getFavoriteTeam();
        if (favoriteTeam == null) return;

        String shortName = favoriteTeam.getShortName();
        String abbreviation = favoriteTeam.getAbbreviation();

        games.sort((a, b) -> {
            boolean aMatch = isTeamMatch(a, shortName, abbreviation);
            boolean bMatch = isTeamMatch(b, shortName, abbreviation);
            if (aMatch && !bMatch) return -1;
            if (!aMatch && bMatch) return 1;
            return 0;
        });
    }

    private boolean isTeamMatch(GameScoreResponse game, String shortName, String abbreviation) {
        return game.awayTeam().equals(shortName) || game.homeTeam().equals(shortName)
                || game.awayTeam().equals(abbreviation) || game.homeTeam().equals(abbreviation);
    }

    private GameScoreResponse toGameScoreResponse(JsonNode game, WeatherInfo weather) {
        String stateCode = game.path("GAME_STATE_SC").asText();
        boolean isLive = "2".equals(stateCode);
        boolean isFinished = "3".equals(stateCode);
        String gameState = switch (stateCode) {
            case "1" -> "경기 전";
            case "2" -> "진행중";
            case "3" -> "종료";
            default -> "알 수 없음";
        };

        String savePitcher = game.path("SV_PIT_P_NM").asText().trim();

        return new GameScoreResponse(
                game.path("G_DT_TXT").asText(),
                game.path("G_TM").asText(),
                game.path("S_NM").asText(),
                game.path("AWAY_NM").asText(),
                game.path("HOME_NM").asText(),
                game.path("T_SCORE_CN").asText(),
                game.path("B_SCORE_CN").asText(),
                isLive,
                isFinished,
                gameState,
                game.path("GAME_INN_NO").asInt(),
                game.path("GAME_TB_SC_NM").asText(),
                game.path("CANCEL_SC_NM").asText(),
                game.path("W_PIT_P_NM").asText().trim(),
                game.path("L_PIT_P_NM").asText().trim(),
                savePitcher.isBlank() ? null : savePitcher,
                weather
        );
    }
}
