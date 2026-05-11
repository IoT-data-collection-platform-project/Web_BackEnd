package com.iot_sw.iot_web_backend.dashboard.dto;

import java.time.LocalDate;
import java.util.List;

public record HistoryResponseDto(
        String period,
        LocalDate baseDate,
        String macAddress,
        List<HistoryPointDto> points
) {
}
