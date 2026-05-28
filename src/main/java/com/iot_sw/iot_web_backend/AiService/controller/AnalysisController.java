package com.iot_sw.iot_web_backend.AiService.controller;

import com.iot_sw.iot_web_backend.AiService.dto.request.AnalysisRequestDto;
import com.iot_sw.iot_web_backend.AiService.entity.AiAnalysis;
import com.iot_sw.iot_web_backend.AiService.entity.AiReport;
import com.iot_sw.iot_web_backend.AiService.repository.AiAnalysisRepository;
import com.iot_sw.iot_web_backend.AiService.repository.AiReportRepository;
import com.iot_sw.iot_web_backend.AiService.service.AnalysisService;
import com.iot_sw.iot_web_backend.dashboard.entity.WeatherData;
import com.iot_sw.iot_web_backend.dashboard.repository.WeatherRepository;
import com.iot_sw.iot_web_backend.device.entity.SensorTelemetry;
import com.iot_sw.iot_web_backend.device.repository.DeviceRepository;
import com.iot_sw.iot_web_backend.device.repository.SensorRepository;
import com.iot_sw.iot_web_backend.setting.entity.ControlStatus;
import com.iot_sw.iot_web_backend.setting.entity.Environment;
import com.iot_sw.iot_web_backend.setting.repository.ControlStatusRepository;
import com.iot_sw.iot_web_backend.setting.repository.EnvironmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AnalysisController {
    private final AnalysisService analysisService;
    private final AiAnalysisRepository aiAnalysisRepository;
    private final AiReportRepository aiReportRepository;
    private final DeviceRepository deviceRepository;
    private final SensorRepository sensorRepository;
    private final WeatherRepository weatherRepository;
    private final EnvironmentRepository environmentRepository;
    private final ControlStatusRepository controlStatusRepository;

    @GetMapping("/latest")
    public ResponseEntity<Map<String, Object>> latest(@RequestParam(required = false) String mac) {
        Optional<AiAnalysis> latest = (mac != null && !mac.isBlank())
                ? aiAnalysisRepository.findTopByDevice_MacIdOrderByCreatedAtDesc(mac.trim())
                : aiAnalysisRepository.findTopByOrderByCreatedAtDesc();

        if (latest.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        AiAnalysis analysis = latest.get();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("macAddress", analysis.getDevice().getMacId());
        response.put("status", analysis.getAnalysis().get("status"));
        response.put("summary", analysis.getAnalysis().get("summary"));
        response.put("createdAt", analysis.getCreatedAt());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/latest")
    public ResponseEntity<Map<String, Object>> latestReport(@RequestParam(required = false) String mac) {
        Optional<AiReport> latest = (mac != null && !mac.isBlank())
                ? aiReportRepository.findTopByDevice_MacIdOrderByCreatedAtDesc(mac.trim())
                : aiReportRepository.findTopByOrderByCreatedAtDesc();

        if (latest.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        AiReport report = latest.get();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("macAddress", report.getDevice().getMacId());
        response.put("result", report.getResult());
        response.put("createdAt", report.getCreatedAt());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/report/daily")
    public ResponseEntity<Map<String, Object>> dailyReport(
            @RequestParam(required = false) String mac,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        Optional<AiReport> daily = (mac != null && !mac.isBlank())
                ? aiReportRepository.findTopByDevice_MacIdAndCreatedAtBetweenOrderByCreatedAtDesc(mac.trim(), start, end)
                : aiReportRepository.findTopByCreatedAtBetweenOrderByCreatedAtDesc(start, end);

        if (daily.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        AiReport report = daily.get();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("macAddress", report.getDevice().getMacId());
        response.put("result", report.getResult());
        response.put("createdAt", report.getCreatedAt());
        response.put("reportDate", date.toString());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reanalyze")
    public ResponseEntity<Map<String, Object>> reanalyze(@RequestParam String mac) {
        String normalizedMac = mac == null ? "" : mac.trim();
        if (normalizedMac.isBlank()) {
            return ResponseEntity.badRequest().body(messageBody("mac 파라미터가 필요합니다."));
        }

        return deviceRepository.findByMacId(normalizedMac)
                .map(device -> {
                    Long deviceId = device.getId();

                    Optional<SensorTelemetry> latestSensorOpt = sensorRepository.findTopByDevice_MacIdOrderByMeasuredAtDesc(normalizedMac);
                    Optional<WeatherData> latestWeatherOpt = weatherRepository.findTopByOrderByCreatedAtDesc();
                    Optional<Environment> envOpt = environmentRepository.findByDeviceId(deviceId);
                    Optional<ControlStatus> controlOpt = controlStatusRepository.findByDeviceId(deviceId);

                    if (latestSensorOpt.isEmpty() || envOpt.isEmpty() || controlOpt.isEmpty()) {
                        return ResponseEntity.badRequest().body(messageBody("재분석에 필요한 센서/환경/제어 데이터가 부족합니다."));
                    }

                    AnalysisRequestDto requestDto = buildRequest(
                            normalizedMac,
                            latestSensorOpt.get(),
                            latestWeatherOpt.orElse(null),
                            envOpt.get(),
                            controlOpt.get()
                    );

                    analysisService.requestAiAnalysis(deviceId, requestDto);
                    Map<String, Object> body = new LinkedHashMap<>();
                    body.put("message", "재분석 요청을 접수했습니다.");
                    body.put("macAddress", normalizedMac);
                    body.put("requestedAt", LocalDateTime.now().toString());
                    return ResponseEntity.accepted().body(body);
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(messageBody("등록되지 않은 MAC 주소입니다.")));
    }

    private AnalysisRequestDto buildRequest(
            String mac,
            SensorTelemetry sensor,
            WeatherData weather,
            Environment env,
            ControlStatus control
    ) {
        // 날씨 데이터 조립
        AnalysisRequestDto.Outdoor outdoorDto;
        if (weather != null) {
            outdoorDto = AnalysisRequestDto.Outdoor.builder()
                    .ta(weather.getTempTa() != null ? weather.getTempTa() : 0.0)
                    .wd(weather.getWindDirWd() != null ? weather.getWindDirWd() : 0.0)
                    .ws(weather.getWindSpeedWs() != null ? weather.getWindSpeedWs() : 0.0)
                    .hm(weather.getHumidityHm() != null ? weather.getHumidityHm() : 0.0)
                    .rn(weather.getPrecipitationRn() != null ? weather.getPrecipitationRn() : 0.0)
                    .isSW(weather.getIsStrongWindWarning() != null && weather.getIsStrongWindWarning() > 0)
                    .isDW(weather.getIsDryWarning() != null && weather.getIsDryWarning() > 0)
                    .build();
        } else {
            log.warn("[AI] MAC: {} - 최신 날씨 데이터를 찾을 수 없어 기본값(0.0)으로 진행합니다.", mac);
            outdoorDto = AnalysisRequestDto.Outdoor.builder()
                    .ta(0.0).wd(0.0).ws(0.0).hm(0.0).rn(0.0)
                    .isSW(false).isDW(false)
                    .build();
        }

        // DTO 조립
        return AnalysisRequestDto.builder()
                .macAddress(mac)

                // 실내 데이터
                .indoor(AnalysisRequestDto.Indoor.builder()
                        .temperature(toDouble(sensor.getTemperature()))
                        .humidity(toDouble(sensor.getHumidity()))
                        .pressure(toDouble(sensor.getPressure()))
                        .tvoc(sensor.getTvoc() != null ? sensor.getTvoc() : 0)
                        .eco2(sensor.getEco2() != null ? sensor.getEco2() : 0)
                        .flame(sensor.getFlameValue() != null ? sensor.getFlameValue() : 0)
                        .build())

                // 날씨 데이터
                .outdoor(outdoorDto)

                // 인프라 유무
                .setting(AnalysisRequestDto.Setting.builder()
                        .north_window(Boolean.TRUE.equals(env.getNorthWindow()))
                        .south_window(Boolean.TRUE.equals(env.getSouthWindow()))
                        .east_window(Boolean.TRUE.equals(env.getEastWindow()))
                        .west_window(Boolean.TRUE.equals(env.getWestWindow()))
                        .air_conditioner(Boolean.TRUE.equals(env.getAirConditioner()))
                        .heating(Boolean.TRUE.equals(env.getHeating()))
                        .humidifier(Boolean.TRUE.equals(env.getHumidifier()))
                        .dehumidifier(Boolean.TRUE.equals(env.getDehumidifier()))
                        .air_cleaner(Boolean.TRUE.equals(env.getAirCleaner()))
                        .sprinkler(Boolean.TRUE.equals(env.getSprinkler()))
                        .fire_alarm(Boolean.TRUE.equals(env.getFireAlarm()))
                        .build())

                // 인프라 현재 제어 상태
                .control(AnalysisRequestDto.Control.builder()
                        .north_window(Boolean.TRUE.equals(control.getNorthWindow()))
                        .south_window(Boolean.TRUE.equals(control.getSouthWindow()))
                        .east_window(Boolean.TRUE.equals(control.getEastWindow()))
                        .west_window(Boolean.TRUE.equals(control.getWestWindow()))
                        .air_conditioner(Boolean.TRUE.equals(control.getAirConditioner()))
                        .heating(Boolean.TRUE.equals(control.getHeating()))
                        .humidifier(Boolean.TRUE.equals(control.getHumidifier()))
                        .dehumidifier(Boolean.TRUE.equals(control.getDehumidifier()))
                        .air_cleaner(Boolean.TRUE.equals(control.getAirCleaner()))
                        .sprinkler(Boolean.TRUE.equals(control.getSprinkler()))
                        .fire_alarm(Boolean.TRUE.equals(control.getFireAlarm()))
                        .build())

                .alert(AnalysisRequestDto.Alert.builder()
                        .category("UNKNOWN")
                        .severity("UNKNOWN")
                        .build())
                .build();
    }

    private Double toDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private boolean isEnabled(Boolean value) {
        return Boolean.TRUE.equals(value);
    }

    private Map<String, Object> messageBody(String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", message);
        return body;
    }
}
