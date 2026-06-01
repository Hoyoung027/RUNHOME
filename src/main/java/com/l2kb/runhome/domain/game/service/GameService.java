package com.l2kb.runhome.domain.game.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.l2kb.runhome.domain.game.dto.GameScoreResponse;
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
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    public List<GameScoreResponse> getGameList(String date, Long userId) {
        String targetDate = (date != null && !date.isBlank())
                ? date
                : LocalDate.now().format(DATE_FORMAT);

        String body = "leId=1&srId=0&date=" + targetDate;

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

            Map<String, WeatherInfo> weatherCache = new HashMap<>();
            List<GameScoreResponse> games = new ArrayList<>();

            for (JsonNode game : root.path("game")) {
                String stadiumId = game.path("S_ID").asText();
                WeatherInfo weather = weatherCache.computeIfAbsent(stadiumId, this::fetchWeather);
                games.add(toGameScoreResponse(game, weather));
            }

            sortByFavoriteTeam(games, userId);
            return games;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
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
