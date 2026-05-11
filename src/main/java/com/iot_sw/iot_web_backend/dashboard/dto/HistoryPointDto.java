package com.iot_sw.iot_web_backend.dashboard.dto;

import java.time.LocalDateTime;

public record HistoryPointDto(
        String label,
        LocalDateTime bucketStart,
        LocalDateTime bucketEnd,
        Double indoorTemp,
        Double outdoorTemp,
        Double indoorHumidity
) {
}
