package com.sw.project.service;

import com.sw.project.dto.WeatherDTO;
import com.sw.project.dto.WeatherResponseDto;
import com.sw.project.entity.WeatherData;
import com.sw.project.repository.WeatherRepository;
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