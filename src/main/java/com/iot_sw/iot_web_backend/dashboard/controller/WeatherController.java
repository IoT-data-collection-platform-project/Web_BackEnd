package com.iot_sw.iot_web_backend.dashboard.controller;

import com.iot_sw.iot_web_backend.dashboard.dto.WeatherResponseDto;
import com.iot_sw.iot_web_backend.dashboard.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public WeatherResponseDto getWeather() {
        return weatherService.getWeather();
    }
}