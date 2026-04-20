package com.iot_sw.iot_web_backend.dashboard.config;

import com.iot_sw.iot_web_backend.dashboard.service.PublicApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "weather.fetch.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class WeatherStartupRunner {

    private final PublicApiService publicApiService;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        publicApiService.fetchAndSave();
    }
}
