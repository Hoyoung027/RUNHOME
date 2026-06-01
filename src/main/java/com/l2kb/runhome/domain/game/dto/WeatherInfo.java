package com.l2kb.runhome.domain.game.dto;

public record WeatherInfo(
        String condition,
        double temperature,
        String humidity,
        String windDirection,
        double windSpeed,
        String iconUrl
) {
}
