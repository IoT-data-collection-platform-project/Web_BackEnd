package com.iot_sw.iot_web_backend.dashboard.service;

import com.iot_sw.iot_web_backend.dashboard.dto.WeatherDTO;
import com.iot_sw.iot_web_backend.dashboard.dto.WeatherResponseDto;
import com.iot_sw.iot_web_backend.dashboard.entity.WeatherData;
import com.iot_sw.iot_web_backend.dashboard.repository.WeatherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherRepository weatherRepository;
    private final WeatherAlertService alertService;

    public WeatherResponseDto getWeather() {

        List<WeatherData> list =
                weatherRepository.findAllByOrderByTmDesc();

        boolean[] alert = alertService.getAlert();

        List<WeatherDTO> dtoList = list.stream()
                .map(w -> new WeatherDTO(
                        w.getTm(),
                        w.getWd(),
                        w.getWs(),
                        w.getTa(),
                        w.getHm(),
                        w.getRn()
                ))
                .toList();

        WeatherResponseDto dto = new WeatherResponseDto();
        dto.setWeather(dtoList);
        dto.setWindWarning(alert[0]);
        dto.setDryWarning(alert[1]);

        return dto;
    }
}