package com.iot_sw.iot_web_backend.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class WeatherAlertService {

    private static final DateTimeFormatter TM_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final RestTemplate restTemplate;

    @Value("${kma.warning.now.url}")
    private String warningNowUrl;

    @Value("${kma.warning.fe}")
    private String warningFe;

    @Value("${kma.warning.authKey}")
    private String warningAuthKey;

    public boolean[] getAlert() {
        String tm = LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(TM_FORMATTER);
        String requestUrl = String.format("%s?fe=%s&tm=%s&disp=0&authKey=%s", warningNowUrl, warningFe, tm, warningAuthKey);

        if (warningAuthKey == null || warningAuthKey.isBlank()) {
            return new boolean[]{false, false};
        }
        try {
            String response =
                    restTemplate.getForObject(requestUrl, String.class);

            if (response != null) {
                return parse(response);
            }

        } catch (Exception e) {
            System.err.println("특보 API 호출 오류: " + e.getMessage());
        }

        return new boolean[]{false, false};
    }

    private boolean[] parse(String response) {

        boolean wind = false;
        boolean dry = false;

        String[] lines = response.split("\n");

        for (String line : lines) {
            line = line.trim();

            if (line.startsWith("#") || line.isEmpty()) {
                continue;
            }

            String[] tokens = line.split("\\s+");
            for (String token : tokens) {
                if ("W".equalsIgnoreCase(token)) {
                    wind = true;
                } else if ("D".equalsIgnoreCase(token)) {
                    dry = true;
                }
            }

            if (wind && dry) {
                break;
            }
        }

        return new boolean[]{wind, dry};
    }
}