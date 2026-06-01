package com.l2kb.runhome.domain.ranking.dto;

import java.util.List;

public record TeamRankingResponse(String title, List<TeamRankResponse> rankings) {
}
