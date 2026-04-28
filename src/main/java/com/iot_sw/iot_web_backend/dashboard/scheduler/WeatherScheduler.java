package com.iot_sw.iot_web_backend.dashboard.scheduler;

import com.iot_sw.iot_web_backend.dashboard.service.PublicApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "weather.fetch.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class WeatherScheduler {

    private final PublicApiService apiService;

    @EventListener(ApplicationReadyEvent.class)
    public void runOnStartup() {
        apiService.fetchAndSave();
    }

    @Scheduled(cron = "${weather.fetch.cron:0 0 * * * *}")
    public void run() {
        apiService.fetchAndSave();
    }
}