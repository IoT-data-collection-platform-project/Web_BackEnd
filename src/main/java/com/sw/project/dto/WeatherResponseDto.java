package com.sw.project.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WeatherResponseDto {

    private List<WeatherDTO> weather;

    private boolean windWarning;
    private boolean dryWarning;
}