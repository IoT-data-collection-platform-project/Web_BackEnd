package com.iot_sw.iot_web_backend.device.service;

import com.iot_sw.iot_web_backend.device.dto.request.RegisterRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.stereotype.Service;
// ✅ 올바른 임포트 구문 (이걸 넣어주세요)
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceMqttService {

    private final DeviceService deviceService; // 💡 Repository 대신 Service를 호출!
    private final ObjectMapper objectMapper;

    @ServiceActivator(inputChannel = "mqttInboundChannel")
    public void handleMessage(String payload, @Header(MqttHeaders.RECEIVED_TOPIC) String topic) {
        log.info("📩 MQTT 수신 - 토픽: {}, 내용: {}", topic, payload);

        try {
            if (topic.equals("provisioning/request")) {
                JsonNode json = objectMapper.readTree(payload);

                //String macId = json.get("mac_address").asString();
                //String location = json.has("location") ? json.get("location").asString() : "위치 미지정";
                //String ipAddress = json.has("ip_address") ? json.get("ip_address").asString() : "0.0.0.0";

                // 💡 핵심 비즈니스 로직은 Service에게 맡깁니다.
                deviceService.registerPendingDevice(RegisterRequestDTO.builder()
                                                    .macId(json.get("mac_address").asText())
                                                    .ipAddress(json.has("ip_address") ? json.get("ip_address").asText() : "0.0.0.0")
                                                    .build());
            }
            else if (topic.startsWith("telemetry/")) {
                // 센서 데이터 처리 로직 위임 예정
            }
        } catch (Exception e) {
            log.error("MQTT 파싱/처리 중 오류: {}", e.getMessage());
        }
    }
}