package com.iot_sw.iot_web_backend.dashboard.controller;

import com.iot_sw.iot_web_backend.dashboard.dto.HistoryResponseDto;
import com.iot_sw.iot_web_backend.dashboard.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;

    @GetMapping
    public HistoryResponseDto getHistory(
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(required = false, defaultValue = "") String mac
    ) {
        return historyService.getHistory(period, mac);
    }
}
