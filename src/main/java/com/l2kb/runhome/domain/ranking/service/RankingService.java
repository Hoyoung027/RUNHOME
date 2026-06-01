package com.l2kb.runhome.domain.ranking.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.l2kb.runhome.domain.ranking.dto.*;

import com.l2kb.runhome.global.exception.BusinessException;
import com.l2kb.runhome.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RestClient restClient;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String KBO_RANK_URL = "https://m.koreabaseball.com/ws/Kbo.asmx/GetTeamRank";
    private static final String KBO_HITTER_URL = "https://m.koreabaseball.com/ws/Kbo.asmx/GetHitterTop5";
    private static final String KBO_PITCHER_URL = "https://m.koreabaseball.com/ws/Kbo.asmx/GetPitcherTop5";

    public TeamRankingResponse getTeamRankings() {
        String body = "season_id=" + LocalDate.now().getYear() + "&sr_id=0";

        try {
            String response = restClient.post()
                    .uri(KBO_RANK_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Mobile Safari/537.36")
                    .header("Referer", "https://m.koreabaseball.com/")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = MAPPER.readTree(response);
            String title = root.path("title").asText();
            List<TeamRankResponse> rankings = new ArrayList<>();

            for (JsonNode rowWrapper : root.path("rows")) {
                JsonNode row = rowWrapper.path("row");
                rankings.add(new TeamRankResponse(
                        Integer.parseInt(row.get(0).path("Text").asText()),
                        row.get(1).path("Text").asText().replaceAll("<[^>]+>", ""),
                        Integer.parseInt(row.get(2).path("Text").asText()),
                        Integer.parseInt(row.get(3).path("Text").asText()),
                        Integer.parseInt(row.get(4).path("Text").asText()),
                        Integer.parseInt(row.get(5).path("Text").asText()),
                        row.get(6).path("Text").asText(),
                        row.get(7).path("Text").asText(),
                        row.get(8).path("Text").asText()
                ));
            }

            return new TeamRankingResponse(title, rankings);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    public HitterTopResponse getHitterTop5() {
        String body = "season_id=" + LocalDate.now().getYear() + "&sr_id=0";

        try {
            String response = restClient.post()
                    .uri(KBO_HITTER_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Mobile Safari/537.36")
                    .header("Referer", "https://m.koreabaseball.com/")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = MAPPER.readTree(response);
            String title = root.path("update").asText();
            List<HitterStatCategory> records = new ArrayList<>();

            for (JsonNode category : root.path("record")) {
                String statName = category.path("text").asText();
                List<HitterRecord> top5 = new ArrayList<>();
                for (JsonNode item : category.path("item")) {
                    top5.add(new HitterRecord(
                            item.path("RANK_NO").asInt(),
                            item.path("P_NM").asText(),
                            item.path("T_NM").asText(),
                            item.path("RECORD_VA").asText()
                    ));
                }
                records.add(new HitterStatCategory(statName, top5));
            }

            return new HitterTopResponse(title, records);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    public PitcherTopResponse getPitcherTop5() {
        String body = "season_id=" + LocalDate.now().getYear() + "&sr_id=0";

        try {
            String response = restClient.post()
                    .uri(KBO_PITCHER_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Mobile Safari/537.36")
                    .header("Referer", "https://m.koreabaseball.com/")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = MAPPER.readTree(response);
            String title = root.path("update").asText();
            List<HitterStatCategory> records = new ArrayList<>();

            for (JsonNode category : root.path("record")) {
                String statName = category.path("text").asText();
                List<HitterRecord> top5 = new ArrayList<>();
                for (JsonNode item : category.path("item")) {
                    top5.add(new HitterRecord(
                            item.path("RANK_NO").asInt(),
                            item.path("P_NM").asText(),
                            item.path("T_NM").asText(),
                            item.path("RECORD_VA").asText()
                    ));
                }
                records.add(new HitterStatCategory(statName, top5));
            }

            return new PitcherTopResponse(title, records);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }
}
