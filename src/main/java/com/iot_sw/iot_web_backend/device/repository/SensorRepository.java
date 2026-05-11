package com.iot_sw.iot_web_backend.device.repository;

import com.iot_sw.iot_web_backend.device.entity.SensorTelemetry;
import com.iot_sw.iot_web_backend.device.entity.SensorTelemetryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SensorRepository extends JpaRepository<SensorTelemetry, SensorTelemetryId> {
    List<SensorTelemetry> findByMeasuredAtBetweenOrderByMeasuredAtAsc(LocalDateTime start, LocalDateTime end);

    List<SensorTelemetry> findByMacAddressAndMeasuredAtBetweenOrderByMeasuredAtAsc(
            String macAddress,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<SensorTelemetry> findTopByOrderByMeasuredAtDesc();

    Optional<SensorTelemetry> findTopByMacAddressOrderByMeasuredAtDesc(String macAddress);
}