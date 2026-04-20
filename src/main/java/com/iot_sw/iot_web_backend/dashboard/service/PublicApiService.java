package com.iot_sw.iot_web_backend.dashboard.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.iot_sw.iot_web_backend.dashboard.entity.WeatherData;
import com.iot_sw.iot_web_backend.dashboard.repository.WeatherRepository;

@Service
@RequiredArgsConstructor // final이 붙은 필드들을 파라미터로 받는 생성자를 생성합니다.
public class PublicApiService {
    private static final DateTimeFormatter TM_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final WeatherRepository weatherRepository;
    
    // RestTemplate을 직접 생성하지 않고 생성자 주입을 받도록 변경합니다.
    private final RestTemplate restTemplate;

    @Value("${kma.url}")
    private String url;

    @Value("${kma.authKey}")
    private String authKey;

    @Value("${kma.stn}")
    private String stn;

    public void fetchAndSave() {
        ZoneId seoul = ZoneId.of("Asia/Seoul");
        LocalDateTime base = LocalDateTime.now(seoul).withMinute(0).withSecond(0).withNano(0);

        for (int back = 0; back < 48; back++) {
            LocalDateTime candidate = base.minusHours(back);
            String tm = candidate.format(TM_FORMATTER);
            if (weatherRepository.existsByTm(tm)) {
                continue;
            }
            List<WeatherData> list = fetchForTm(tm);
            if (!list.isEmpty()) {
                weatherRepository.saveAll(list);
                return;
            }
        }
    }

    private List<WeatherData> fetchForTm(String tm) {
        String requestUrl = String.format("%s?tm=%s&stn=%s&help=0&authKey=%s", url, tm, stn, authKey);
        try {
            String response = restTemplate.getForObject(requestUrl, String.class);
            if (response != null) {
                return parse(response);
            }
        } catch (Exception e) {
            System.err.println("API 호출 중 오류 발생 (tm=" + tm + "): " + e.getMessage());
        }
        return List.of();
    }

    private List<WeatherData> parse(String response) {
        List<WeatherData> list = new ArrayList<>();
        String[] lines = response.split("\n");

        for (String line : lines) {
            line = line.trim();

            // 헤더 및 불필요한 라인 제외
            if (line.startsWith("#") || line.startsWith("TM") || line.isEmpty()) {
                continue;
            }

            String[] data = line.split("\\s+");

            // TM STN WD WS ... TA(11) TD(12) HM(13) PV(14) RN(15)
            if (data.length < 16) continue;

            try {
                WeatherData weather = WeatherData.builder()
                        .tm(data[0])
                        .wd(Double.parseDouble(data[2]))
                        .ws(Double.parseDouble(data[3]))
                        .ta(Double.parseDouble(data[11]))
                        .hm(Double.parseDouble(data[13]))
                        .rn(Double.parseDouble(data[15]))
                        .build();

                list.add(weather);
            } catch (NumberFormatException e) {
                // 숫자 파싱 실패 시 해당 라인만 건너뜁니다.
                continue;
            }
        }
        return list;
    }
}