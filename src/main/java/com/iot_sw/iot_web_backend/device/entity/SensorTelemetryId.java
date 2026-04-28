package com.iot_sw.iot_web_backend.device.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SensorTelemetryId implements Serializable {
    private String macAddress;
    private LocalDateTime measuredAt;
}
