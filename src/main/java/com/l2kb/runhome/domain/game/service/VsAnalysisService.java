package com.l2kb.runhome.domain.game.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.l2kb.runhome.domain.game.dto.*;
import com.l2kb.runhome.domain.user.entity.User;
import com.l2kb.runhome.domain.user.repository.UserRepository;
import com.l2kb.runhome.global.exception.BusinessException;
import com.l2kb.runhome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class VsAnalysisService {

    private final RestClient restClient;
    private final UserRepository userRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String KBO_GAME_URL = "https://m.koreabaseball.com/ws/Kbo.asmx/GetKboGameList";
    private static final String KBO_VS_TEAM_URL = "https://www.koreabaseball.com/ws/Main.asmx/GetVsTeam";
    private static final String KBO_VS_KEY_PLAYER_URL = "https://www.koreabaseball.com/ws/Main.asmx/GetVsTeamKeyPlayer";
    private static final String KBO_LINEUP_ANALYSIS_URL = "https://www.koreabaseball.com/ws/Main.asmx/GetLineUpAnalysis";

    private static final Map<String, String> DB_TO_KBO = Map.of(
            "KIA", "HT", "LG", "LG", "DOOSAN", "OB", "SAMSUNG", "SS", "KT", "KT",
            "SSG", "SK", "LOTTE", "LT", "HANWHA", "HH", "NC", "NC", "KIWOOM", "WO"
    );

    private static final Pattern SPAN_TEXT_PATTERN = Pattern.compile("<span>([^<]+)</span>");
    private static final Pattern TEAM_LOGO_PATTERN = Pattern.compile("<img class='team' src='([^']+)'");
    private static final Pattern PLAYER_IMG_PATTERN = Pattern.compile("<img src='([^']+)' onerror");
    private static final Pattern PLAYER_NAME_PATTERN = Pattern.compile("<span class='name'>([^<]+)</span>");
    private static final Pattern WIN_RATE_PATTERN = Pattern.compile("</span>([\\d.]+%)");

    // ── 팀 전력 비교 ────────────────────────────────────────────

    public List<TeamComparisonResponse> getTeamComparison(Long userId) {
        List<JsonNode> games = findNextScheduledGames(resolveKboTeamId(userId));
        return games.stream()
                .map(this::toTeamComparisonResponse)
                .filter(Objects::nonNull)
                .toList();
    }

    private TeamComparisonResponse toTeamComparisonResponse(JsonNode game) {
        String gameId = game.path("G_ID").asText();
        String awayId = game.path("AWAY_ID").asText();
        String homeId = game.path("HOME_ID").asText();
        String body = "leId=1&srId=0&seasonId=" + LocalDate.now().getYear()
                + "&awayTeamId=" + awayId + "&homeTeamId=" + homeId + "&gameId=" + gameId;
        try {
            String response = callKboApi(KBO_VS_TEAM_URL, body);
            JsonNode root = MAPPER.readTree(response);
            String title = root.path("title").asText();
            JsonNode rows = MAPPER.readTree(root.path("season").asText()).path("rows");

            return new TeamComparisonResponse(
                    gameId,
                    game.path("G_DT_TXT").asText(),
                    game.path("G_TM").asText(),
                    game.path("S_NM").asText(),
                    game.path("AWAY_NM").asText(),
                    game.path("HOME_NM").asText(),
                    title,
                    parseTeamStats(rows.get(0).path("row")),
                    parseTeamStats(rows.get(1).path("row"))
            );
        } catch (Exception e) {
            log.warn("팀 전력 비교 조회 실패: gameId={}", gameId);
            return null;
        }
    }

    private TeamStats parseTeamStats(JsonNode row) {
        String teamHtml = row.get(0).path("Text").asText();
        Matcher m = SPAN_TEXT_PATTERN.matcher(teamHtml);
        String teamName = "";
        while (m.find()) teamName = m.group(1);

        String era = row.get(1).path("Text").asText();
        boolean eraWin = "win".equals(row.get(1).path("Class").asText());
        String ba = row.get(2).path("Text").asText();
        boolean baWin = "win".equals(row.get(2).path("Class").asText());
        String runsScored = row.get(3).path("Text").asText();
        boolean runsScoredWin = "win".equals(row.get(3).path("Class").asText());
        String runsAllowed = row.get(4).path("Text").asText();

        return new TeamStats(teamName, era, eraWin, ba, baWin, runsScored, runsScoredWin, runsAllowed);
    }

    // ── 키플레이어 비교 ────────────────────────────────────────────

    public List<KeyPlayersResponse> getKeyPlayers(Long userId) {
        List<JsonNode> games = findNextScheduledGames(resolveKboTeamId(userId));
        return games.stream()
                .map(this::toKeyPlayersResponse)
                .filter(Objects::nonNull)
                .toList();
    }

    private KeyPlayersResponse toKeyPlayersResponse(JsonNode game) {
        String gameId = game.path("G_ID").asText();
        String awayId = game.path("AWAY_ID").asText();
        String homeId = game.path("HOME_ID").asText();
        String body = "leId=1&srId=0&seasonId=" + LocalDate.now().getYear()
                + "&awayTeamId=" + awayId + "&homeTeamId=" + homeId + "&gameId=" + gameId;
        try {
            String response = callKboApi(KBO_VS_KEY_PLAYER_URL, body);
            JsonNode root = MAPPER.readTree(response);
            JsonNode rows = MAPPER.readTree(root.path("keyplayer").asText()).path("rows");

            return new KeyPlayersResponse(
                    gameId,
                    game.path("G_DT_TXT").asText(),
                    game.path("G_TM").asText(),
                    game.path("S_NM").asText(),
                    game.path("AWAY_NM").asText(),
                    game.path("HOME_NM").asText(),
                    parseKeyPlayer(rows.get(0).path("row"), rows.get(1).path("row")),
                    parseKeyPlayer(rows.get(2).path("row"), rows.get(3).path("row"))
            );
        } catch (Exception e) {
            log.warn("키플레이어 조회 실패: gameId={}", gameId);
            return null;
        }
    }

    private KeyPlayer parseKeyPlayer(JsonNode headerRow, JsonNode statsRow) {
        String imageHtml = headerRow.get(0).path("Text").asText();

        Matcher logoMatcher = TEAM_LOGO_PATTERN.matcher(imageHtml);
        String teamLogoUrl = logoMatcher.find() ? toHttps(logoMatcher.group(1)) : "";

        Matcher imgMatcher = PLAYER_IMG_PATTERN.matcher(imageHtml);
        String playerImageUrl = imgMatcher.find() ? toHttps(imgMatcher.group(1)) : "";

        String nameRateHtml = headerRow.get(1).path("Text").asText();
        Matcher nameMatcher = PLAYER_NAME_PATTERN.matcher(nameRateHtml);
        String playerName = nameMatcher.find() ? nameMatcher.group(1).trim() : "";

        Matcher rateMatcher = WIN_RATE_PATTERN.matcher(nameRateHtml);
        String recentWinRate = rateMatcher.find() ? rateMatcher.group(1) : "";

        String seasonText = headerRow.get(3).path("Text").asText().replaceAll("<[^>]+>", "").trim();
        Matcher baMatcher = Pattern.compile("[\\d.]+$").matcher(seasonText);
        String seasonBa = baMatcher.find() ? baMatcher.group() : "";

        String recentStats = cleanHtml(statsRow.get(0).path("Text").asText());
        String seasonStats = cleanHtml(statsRow.get(1).path("Text").asText());

        return new KeyPlayer(playerName, teamLogoUrl, playerImageUrl, recentWinRate, recentStats, seasonBa, seasonStats);
    }

    // ── 라인업 WAR 분석 ────────────────────────────────────────────

    public List<LineupWarResponse> getLineupAnalysis(Long userId) {
        List<JsonNode> games = findNextScheduledGames(resolveKboTeamId(userId));
        return games.stream()
                .map(this::toLineupWarResponse)
                .filter(Objects::nonNull)
                .toList();
    }

    private LineupWarResponse toLineupWarResponse(JsonNode game) {
        String gameId = game.path("G_ID").asText();
        String body = "leId=1&srId=0&seasonId=" + LocalDate.now().getYear() + "&gameId=" + gameId;
        try {
            String response = callKboApi(KBO_LINEUP_ANALYSIS_URL, body);
            JsonNode root = MAPPER.readTree(response);

            boolean lineupConfirmed = root.path("CHECK").get(0).path("LINEUP_CK").asBoolean();
            JsonNode warArray = root.path("WAR");

            return new LineupWarResponse(
                    gameId,
                    game.path("G_DT_TXT").asText(),
                    game.path("G_TM").asText(),
                    game.path("S_NM").asText(),
                    game.path("AWAY_NM").asText(),
                    game.path("HOME_NM").asText(),
                    lineupConfirmed,
                    toTeamWarStats(warArray.get(0)),
                    toTeamWarStats(warArray.get(1))
            );
        } catch (Exception e) {
            log.warn("라인업 WAR 분석 조회 실패: gameId={}", gameId);
            return null;
        }
    }

    private TeamWarStats toTeamWarStats(JsonNode node) {
        return new TeamWarStats(
                node.path("T_ID").asText(),
                node.path("T_NM").asText(),
                toHttps(node.path("T_INITIAL_LK").asText()),
                node.path("HITTER_12_WAR_RT").asText(),
                node.path("HITTER_35_WAR_RT").asText(),
                node.path("HITTER_69_WAR_RT").asText()
        );
    }

    // ── 공통 유틸 ────────────────────────────────────────────

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
        try {
            String response = restClient.post()
                    .uri(KBO_GAME_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Mobile Safari/537.36")
                    .header("Referer", "https://m.koreabaseball.com/")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .body("leId=1&srId=0&date=" + date)
                    .retrieve()
                    .body(String.class);
            JsonNode root = MAPPER.readTree(response);
            return StreamSupport.stream(root.path("game").spliterator(), false)
                    .collect(Collectors.toList());
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

    private String callKboApi(String url, String body) {
        return restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36")
                .header("Referer", "https://www.koreabaseball.com/")
                .header("X-Requested-With", "XMLHttpRequest")
                .body(body)
                .retrieve()
                .body(String.class);
    }

    private String toHttps(String url) {
        return url.startsWith("//") ? "https:" + url : url;
    }

    private String cleanHtml(String html) {
        return html.replaceAll("<br />", " ").replaceAll("<[^>]+>", "").trim();
    }
}
