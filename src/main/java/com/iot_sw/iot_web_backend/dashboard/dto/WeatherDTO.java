package com.iot_sw.iot_web_backend.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WeatherDTO {

    private String tm;
    private Double wd;
    private Double ws;
    private Double ta;
    private Double hm;
    private Double rn;

}