package com.l2kb.runhome.domain.highlight.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.l2kb.runhome.domain.highlight.dto.HighlightResponse;
import com.l2kb.runhome.domain.team.entity.Team;
import com.l2kb.runhome.domain.team.repository.TeamRepository;
import com.l2kb.runhome.domain.user.entity.User;
import com.l2kb.runhome.domain.user.repository.UserRepository;
import com.l2kb.runhome.global.exception.BusinessException;
import com.l2kb.runhome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HighlightService {

    private final RestClient restClient;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String KBO_HIGHLIGHT_URL = "https://www.koreabaseball.com/ws/Main.asmx/GetMainHighlight";
    private static final String KBO_DETAIL_BASE = "https://www.koreabaseball.com";
    private static final Pattern YOUTUBE_PATTERN = Pattern.compile("youtube\\.com/embed/([A-Za-z0-9_-]{11})");

    private static final Map<String, String> DB_TO_KBO = Map.of(
            "KIA", "HT",
            "LG", "LG",
            "DOOSAN", "OB",
            "SAMSUNG", "SS",
            "KT", "KT",
            "SSG", "SK",
            "LOTTE", "LT",
            "HANWHA", "HH",
            "NC", "NC",
            "KIWOOM", "WO"
    );

    private static final Map<String, String> KBO_TO_DB = Map.of(
            "HT", "KIA",
            "LG", "LG",
            "OB", "DOOSAN",
            "SS", "SAMSUNG",
            "KT", "KT",
            "SK", "SSG",
            "LT", "LOTTE",
            "HH", "HANWHA",
            "NC", "NC",
            "WO", "KIWOOM"
    );

    public List<HighlightResponse> getHighlights(Long userId) {
        String kboTeamId = resolveKboTeamId(userId);
        List<HighlightRow> rows = fetchHighlightRows();

        if (kboTeamId != null) {
            final String teamId = kboTeamId;
            rows = rows.stream()
                    .filter(r -> teamId.equals(r.awayTeamId) || teamId.equals(r.homeTeamId))
                    .toList();
        }

        Map<String, Team> teamByAbbreviation = teamRepository.findAll().stream()
                .collect(Collectors.toMap(Team::getAbbreviation, Function.identity()));

        return rows.stream()
                .map(row -> toResponse(row, teamByAbbreviation))
                .toList();
    }

    private String resolveKboTeamId(Long userId) {
        if (userId == null) return null;
        User user = userRepository.findWithTeamById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getFavoriteTeam() == null) return null;
        return DB_TO_KBO.get(user.getFavoriteTeam().getAbbreviation());
    }

    private List<HighlightRow> fetchHighlightRows() {
        try {
            String response = restClient.post()
                    .uri(KBO_HIGHLIGHT_URL)
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36")
                    .header("Referer", "https://www.koreabaseball.com/")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .retrieve()
                    .body(String.class);

            JsonNode root = MAPPER.readTree(response);
            List<HighlightRow> rows = new ArrayList<>();
            for (JsonNode node : root.path("row")) {
                rows.add(new HighlightRow(
                        node.path("BD_TT").asText(),
                        node.path("PIC_NM").asText(),
                        node.path("URL_LK").asText(),
                        node.path("G_DT").asText(),
                        node.path("AWAY_T_ID").asText(),
                        node.path("HOME_T_ID").asText(),
                        node.path("S_NM").asText()
                ));
            }
            return rows;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private HighlightResponse toResponse(HighlightRow row, Map<String, Team> teamByAbbreviation) {
        String thumbnailUrl = row.picNm.startsWith("//") ? "https:" + row.picNm : row.picNm;
        String youtubeUrl = fetchYoutubeUrl(row.urlLk);

        String awayAbbr = KBO_TO_DB.getOrDefault(row.awayTeamId, row.awayTeamId);
        String homeAbbr = KBO_TO_DB.getOrDefault(row.homeTeamId, row.homeTeamId);
        Team awayTeam = teamByAbbreviation.get(awayAbbr);
        Team homeTeam = teamByAbbreviation.get(homeAbbr);

        return new HighlightResponse(
                row.title,
                thumbnailUrl,
                youtubeUrl,
                row.gameDate,
                awayTeam != null ? awayTeam.getId() : null,
                awayTeam != null ? awayTeam.getShortName() : awayAbbr,
                homeTeam != null ? homeTeam.getId() : null,
                homeTeam != null ? homeTeam.getShortName() : homeAbbr,
                row.stadiumName
        );
    }

    private String fetchYoutubeUrl(String urlLk) {
        try {
            String html = restClient.get()
                    .uri(KBO_DETAIL_BASE + urlLk)
                    .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36")
                    .header("Referer", "https://www.koreabaseball.com/")
                    .retrieve()
                    .body(String.class);

            Matcher matcher = YOUTUBE_PATTERN.matcher(html);
            if (matcher.find()) {
                return "https://www.youtube.com/watch?v=" + matcher.group(1);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private record HighlightRow(
            String title,
            String picNm,
            String urlLk,
            String gameDate,
            String awayTeamId,
            String homeTeamId,
            String stadiumName
    ) {}
}
