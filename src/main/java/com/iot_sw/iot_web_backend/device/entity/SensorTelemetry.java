package com.iot_sw.iot_web_backend.device.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.domain.Persistable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_telemetry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@IdClass(SensorTelemetryId.class) // 복합키 매핑
public class SensorTelemetry implements Persistable<SensorTelemetryId> {

    @Id
    @Column(length = 17)
    private String macAddress;

    @Id
    private LocalDateTime measuredAt;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(precision = 5, scale = 2)
    private BigDecimal humidity;

    @Column(precision = 6, scale = 2)
    private BigDecimal pressure;

    @Column(columnDefinition = "SMALLINT UNSIGNED")
    private Integer tvoc;

    @Column(columnDefinition = "SMALLINT UNSIGNED")
    private Integer eco2;

    @Column(columnDefinition = "SMALLINT")
    private Integer flameValue;

    @Transient
    @Builder.Default
    private boolean isNew = true;

    @Override
    public SensorTelemetryId getId() {
        return new SensorTelemetryId(this.macAddress, this.measuredAt);
    }

    @Override
    public boolean isNew() {
        return this.isNew;
    }
}
