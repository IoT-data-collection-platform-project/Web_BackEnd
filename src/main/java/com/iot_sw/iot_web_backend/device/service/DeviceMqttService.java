package com.iot_sw.iot_web_backend.device.service;

import com.iot_sw.iot_web_backend.device.dto.request.RegisterRequestDTO;
import com.iot_sw.iot_web_backend.device.dto.request.SensorDataDTO;
import com.iot_sw.iot_web_backend.device.dto.request.TurnOffRequestDTO;
import com.iot_sw.iot_web_backend.device.entity.SensorTelemetry;
import com.iot_sw.iot_web_backend.device.repository.SensorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceMqttService {

    private final DeviceService deviceService;
    private final SensorRepository sensorRepository;
    private final ObjectMapper objectMapper;

    private final List<SensorTelemetry> buffer = Collections.synchronizedList(new ArrayList<>());

    // 구독 채널에 들어온 메시지를 처리
    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleMessage(String payload, @Header(MqttHeaders.RECEIVED_TOPIC) String topic) {
        log.info("MQTT 수신 - 토픽: {}, 내용: {}", topic, payload);

        try {
            if (topic.equals("provisioning/request")) {
                JsonNode json = objectMapper.readTree(payload);

                //String macId = json.get("mac_address").asString();
                //String location = json.has("location") ? json.get("location").asString() : "위치 미지정";
                //String ipAddress = json.has("ip_address") ? json.get("ip_address").asString() : "0.0.0.0";

                // 서비스에 동작 위임
                deviceService.registerPendingDevice(RegisterRequestDTO.builder()
                                                    .macId(json.get("mac_address").asText())
                                                    .ipAddress(json.has("ip_address") ? json.get("ip_address").asText() : "0.0.0.0")
                                                    .build());
            }
            else if (topic.equals("devices/status")) {
                JsonNode json = objectMapper.readTree(payload);

                log.info("[MQTT] 게이트웨이 비정상 종료 감지");

                if(json.get("status").asText().equals("OFFLINE")) {
                    deviceService.turnOffDevice(TurnOffRequestDTO.builder()
                            .macId(json.get("mac_address").asText())
                            .status(json.get("status").asText())
                            .build());
                }
            }
            else if (topic.startsWith("gateway/") && topic.endsWith("/telemetry")) {
                // 1. 토픽에서 MAC 주소 추출 (gateway/{mac_address}/telemetry)
                String[] topicParts = topic.split("/");
                if (topicParts.length == 3) {
                    String macAddress = topicParts[1];

                    // 2. JSON Payload를 DTO로 한 번에 직렬화 매핑 (JsonNode 탐색보다 훨씬 빠르고 안전함)
                    SensorDataDTO dto = objectMapper.readValue(payload, SensorDataDTO.class);

                    log.info("[센서 수신] MAC: {}, 온도: {}C, TVOC: {}", macAddress, dto.getTemperature(), dto.getTvoc());

                    // 3. TODO: 센서 데이터 DB 저장 로직 위임
                    // PartitionManagerService에 위임 예정
                    // sensorService.saveTelemetryData(macAddress, sensorData);

                    // 1. 엔티티 변환 (isNew는 기본값 true)
                    SensorTelemetry entity = SensorTelemetry.builder()
                            .macAddress(macAddress)
                            .measuredAt(dto.getMeasuredAt()) // DTO에서 구현한 변환 메서드 활용
                            .temperature(BigDecimal.valueOf(dto.getTemperature()))
                            .humidity(BigDecimal.valueOf(dto.getHumidity()))
                            .pressure(BigDecimal.valueOf(dto.getPressure()))
                            .tvoc(dto.getTvoc())
                            .eco2(dto.getEco2())
                            .flameValue(dto.getFlameValue())
                            .build();

                    // 2. 버퍼에 담기
                    buffer.add(entity);

                    // 3. (옵션) 데이터가 너무 많이 쌓이는 것을 방지하기 위해 60개 넘으면 즉시 플러시
                    if (buffer.size() >= 60) {
                        flushBuffer();
                    }

                    // 4. TODO: 비정상 데이터(화재 감지 등) 실시간 알람 로직
                    // if (sensorData.getFlameValue() < 500) { alertService.triggerFireAlarm(macAddress); }
                }
            }
        } catch (Exception e) {
            log.error("MQTT 파싱/처리 중 오류: {}", e.getMessage());
        }
    }

    // 💡 매 1분(60,000ms)마다 실행되는 스케줄러
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void flushBuffer() {
        if (buffer.isEmpty()) return;

        // 버퍼 복사 후 비우기 (데이터 유실 방지)
        List<SensorTelemetry> toSave;
        synchronized (buffer) {
            toSave = new ArrayList<>(buffer);
            buffer.clear();
        }

        try {
            log.info("[Batch] {}개의 데이터를 MariaDB에 배치 인서트 중...", toSave.size());
            sensorRepository.saveAll(toSave); // 여기서 rewriteBatchedStatements가 작동합니다!
            log.info("[Batch] 저장 완료.");
        } catch (Exception e) {
            log.error("[Batch] 저장 실패: {}", e.getMessage());
        }
    }
}