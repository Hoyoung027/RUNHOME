package com.l2kb.runhome.domain.ranking.dto;

import java.util.List;

public record HitterStatCategory(String statName, List<HitterRecord> top5) {
}
