package com.iot_sw.iot_web_backend.dashboard.scheduler;

import com.iot_sw.iot_web_backend.dashboard.service.PublicApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "weather.fetch.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class WeatherScheduler {

    private final PublicApiService apiService;

    @Scheduled(cron = "0 0 * * * *")
    public void run() {
        apiService.fetchAndSave();
    }
}

/*@Scheduled 쓰려면 필수 설정

@EnableScheduling
@SpringBootApplication
public class ProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProjectApplication.class, args);
    }
} */