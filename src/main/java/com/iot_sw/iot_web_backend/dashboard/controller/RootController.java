package com.iot_sw.iot_web_backend.dashboard.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE + ";charset=UTF-8")
    public String root() {
        return "관측 텍스트: /api/weather";
    }
}
