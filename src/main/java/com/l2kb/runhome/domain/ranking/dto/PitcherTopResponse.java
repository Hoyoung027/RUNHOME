package com.l2kb.runhome.domain.ranking.dto;

import java.util.List;

public record PitcherTopResponse(String title, List<HitterStatCategory> records) {
}
