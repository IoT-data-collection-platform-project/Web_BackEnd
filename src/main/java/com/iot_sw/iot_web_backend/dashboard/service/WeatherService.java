package com.iot_sw.iot_web_backend.dashboard.service;

import com.iot_sw.iot_web_backend.dashboard.dto.WeatherDTO;
import com.iot_sw.iot_web_backend.dashboard.dto.WeatherResponseDto;
import com.iot_sw.iot_web_backend.dashboard.entity.WeatherData;
import com.iot_sw.iot_web_backend.dashboard.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WeatherService {
    private static final DateTimeFormatter TM_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final WeatherRepository weatherRepository;
    private final WeatherAlertService alertService;

    public WeatherResponseDto getWeather() {

        Optional<WeatherData> latest = weatherRepository.findTopByOrderByCreatedAtDesc();

        boolean[] alert = alertService.getAlert();

        List<WeatherDTO> dtoList = latest.stream()
                .map(w -> new WeatherDTO(
                        w.getCreatedAt() != null ? w.getCreatedAt().format(TM_FORMATTER) : null,
                        w.getWindDirWd(),
                        w.getWindSpeedWs(),
                        w.getTempTa(),
                        w.getHumidityHm(),
                        w.getPrecipitationRn()
                ))
                .toList();

        WeatherResponseDto dto = new WeatherResponseDto();
        dto.setWeather(dtoList);
        dto.setWindWarning(alert[0]);
        dto.setDryWarning(alert[1]);

        return dto;
    }
}