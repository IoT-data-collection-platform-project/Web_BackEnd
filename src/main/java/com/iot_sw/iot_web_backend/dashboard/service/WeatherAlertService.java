package com.iot_sw.iot_web_backend.dashboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class WeatherAlertService {

    private final RestTemplate restTemplate;

    @Value("${kma.warning.reg.url}")
    private String warningRegUrl;

    @Value("${kma.warning.type:WD}")
    private String warningType;

    @Value("${kma.warning.reg:108}")
    private String warningRegion;

    @Value("${kma.warning.authKey}")
    private String warningAuthKey;

    public boolean[] getAlert() {
        String requestUrl = String.format(
                "%s?wrn=%s&reg=%s&authKey=%s",
                warningRegUrl,
                warningType,
                warningRegion,
                warningAuthKey
        );

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

            if (line.startsWith("#") || line.isEmpty() || !line.startsWith("L")) {
                continue;
            }

            String[] tokens = line.split("\\s+");
            if (tokens.length < 5) {
                continue;
            }

            String regSp = tokens[3];
            // wrn_reg 응답의 REG_SP 코드에 1(강풍), 2(건조) 포함 여부로 판단
            if (regSp.contains("1")) {
                wind = true;
            }
            if (regSp.contains("2")) {
                dry = true;
            }
            break;
        }

        return new boolean[]{wind, dry};
    }
}