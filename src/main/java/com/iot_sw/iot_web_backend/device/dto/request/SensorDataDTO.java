package com.iot_sw.iot_web_backend.device.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
public class SensorDataDTO {
    private double temperature;
    private double humidity;
    private double pressure;
    private int tvoc;
    private int eco2;
    private int flameValue;

    // C++ 게이트웨이에서 JSON으로 보내는 Unix Timestamp(밀리초)를 그대로 받음
    private long timestamp;

    // 💡 백엔드 서비스(Entity 변환 등)에서 호출할 편의 메서드
    public LocalDateTime getMeasuredAt() {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(this.timestamp),
                ZoneId.of("Asia/Seoul")
        );
    }
}
