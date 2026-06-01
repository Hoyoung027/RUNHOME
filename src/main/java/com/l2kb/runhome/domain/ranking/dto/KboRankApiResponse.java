package com.l2kb.runhome.domain.ranking.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KboRankApiResponse(
        List<RowWrapper> rows,
        String title,
        String code,
        String msg
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RowWrapper(List<Cell> row) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Cell(@JsonProperty("Text") String text) {}
}
