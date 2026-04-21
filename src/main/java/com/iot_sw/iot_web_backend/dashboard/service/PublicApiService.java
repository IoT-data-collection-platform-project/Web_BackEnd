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
    private static final int MIN_BACK_HOURS = 1;

    private final WeatherRepository weatherRepository;
    private final WeatherAlertService weatherAlertService;
    
    // RestTemplate을 직접 생성하지 않고 생성자 주입을 받도록 변경합니다.
    private final RestTemplate restTemplate;

    @Value("${kma.url}")
    private String url;

    @Value("${kma.authKey}")
    private String authKey;

    @Value("${kma.stn}")
    private String stn;

    @Value("${weather.fetch.max-back-hours:3}")
    private int maxBackHours;

    @Value("${weather.fetch.cooldown-minutes:60}")
    private long fetchCooldownMinutes;

    private LocalDateTime lastFetchFailureAt;

    public synchronized void fetchAndSave() {
        if (authKey == null || authKey.isBlank()) {
            System.err.println("kma.authKey가 비어 있어 날씨 API 호출을 건너뜁니다.");
            return;
        }

        ZoneId seoul = ZoneId.of("Asia/Seoul");
        LocalDateTime now = LocalDateTime.now(seoul);
        if (isCooldownActive(now)) {
            return;
        }

        LocalDateTime base = now.withMinute(0).withSecond(0).withNano(0);
        int locationCode = parseLocationCode(stn);
        boolean[] alert = weatherAlertService.getAlert();
        byte isStrongWindWarning = alert[0] ? (byte) 1 : (byte) 0;
        byte isDryWarning = alert[1] ? (byte) 1 : (byte) 0;
        int backHoursToCheck = Math.max(MIN_BACK_HOURS, maxBackHours);
        boolean attemptedExternalCall = false;

        for (int back = 0; back < backHoursToCheck; back++) {
            LocalDateTime candidate = base.minusHours(back);
            String tm = candidate.format(TM_FORMATTER);
            if (weatherRepository.existsByCreatedAtAndLocationCode(candidate, locationCode)) {
                continue;
            }
            attemptedExternalCall = true;
            FetchResult result = fetchForTm(tm, locationCode, isStrongWindWarning, isDryWarning);
            if (result.error()) {
                lastFetchFailureAt = now;
                break;
            }
            if (!result.list().isEmpty()) {
                lastFetchFailureAt = null;
                weatherRepository.saveAll(result.list());
                return;
            }
        }

        if (attemptedExternalCall) {
            // 빈 응답이 반복될 때 다음 주기에서 재호출 폭주를 막기 위해 쿨다운을 건다.
            lastFetchFailureAt = now;
        }
    }

    private FetchResult fetchForTm(String tm, int locationCode, byte isStrongWindWarning, byte isDryWarning) {
        String requestUrl = String.format("%s?tm=%s&stn=%s&help=0&authKey=%s", url, tm, stn, authKey);
        try {
            String response = restTemplate.getForObject(requestUrl, String.class);
            if (response == null || response.isBlank()) {
                System.err.println("[KMA:NO_RESPONSE] tm=" + tm + " 응답 본문이 비어 있습니다.");
                return new FetchResult(List.of(), false);
            }

            if (isLikelyApiErrorResponse(response)) {
                System.err.println("[KMA:API_ERROR_RESPONSE] tm=" + tm + " 응답에 에러 문구가 포함되어 있습니다.");
                return new FetchResult(List.of(), true);
            }

            int rawDataLineCount = countRawDataLines(response);
            List<WeatherData> parsed = parse(response, locationCode, isStrongWindWarning, isDryWarning);
            if (!parsed.isEmpty()) {
                System.out.println("[KMA:DATA_OK] tm=" + tm + " rows=" + parsed.size());
                return new FetchResult(parsed, false);
            }

            if (rawDataLineCount > 0) {
                System.err.println("[KMA:PARSE_EMPTY] tm=" + tm + " 원본 데이터 라인은 있으나 파싱 결과가 0건입니다.");
            } else {
                System.out.println("[KMA:NO_DATA_FOR_TM] tm=" + tm + " 해당 시각 데이터가 없습니다.");
            }
            return new FetchResult(List.of(), false);
        } catch (Exception e) {
            System.err.println("API 호출 중 오류 발생 (tm=" + tm + "): " + e.getMessage());
            return new FetchResult(List.of(), true);
        }
    }

    private List<WeatherData> parse(String response, int locationCode, byte isStrongWindWarning, byte isDryWarning) {
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
                        .locationCode(locationCode)
                        .windDirWd(Double.parseDouble(data[2]))
                        .windSpeedWs(Double.parseDouble(data[3]))
                        .tempTa(Double.parseDouble(data[11]))
                        .humidityHm(Double.parseDouble(data[13]))
                        .precipitationRn(Double.parseDouble(data[15]))
                        .isStrongWindWarning(isStrongWindWarning)
                        .isDryWarning(isDryWarning)
                        .createdAt(LocalDateTime.parse(data[0], TM_FORMATTER))
                        .build();

                list.add(weather);
            } catch (RuntimeException e) {
                // 숫자 파싱 실패 시 해당 라인만 건너뜁니다.
                continue;
            }
        }
        return list;
    }

    private int parseLocationCode(String locationCodeRaw) {
        try {
            return Integer.parseInt(locationCodeRaw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean isLikelyApiErrorResponse(String response) {
        String normalized = response.toUpperCase();
        return normalized.contains("ERROR")
                || normalized.contains("ERR")
                || normalized.contains("AUTH")
                || normalized.contains("SERVICEKEY")
                || normalized.contains("LIMIT")
                || normalized.contains("한도")
                || normalized.contains("인증");
    }

    private int countRawDataLines(String response) {
        int count = 0;
        String[] lines = response.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#") || trimmed.startsWith("TM")) {
                continue;
            }
            count++;
        }
        return count;
    }

    private boolean isCooldownActive(LocalDateTime now) {
        if (lastFetchFailureAt == null) {
            return false;
        }

        if (now.isBefore(lastFetchFailureAt.plusMinutes(fetchCooldownMinutes))) {
            return true;
        }

        lastFetchFailureAt = null;
        return false;
    }

    private record FetchResult(List<WeatherData> list, boolean error) {
    }
}