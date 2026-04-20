package com.iot_sw.iot_web_backend.dashboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.iot_sw.iot_web_backend.dashboard.entity.WeatherData;

import java.time.LocalDateTime;
import java.util.List;

public interface WeatherRepository extends JpaRepository<WeatherData, Long> {

    boolean existsByCreatedAtAndLocationCode(LocalDateTime createdAt, Integer locationCode);

    List<WeatherData> findAllByOrderByCreatedAtDesc();
}