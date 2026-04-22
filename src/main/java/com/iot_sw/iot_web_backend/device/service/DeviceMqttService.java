package com.iot_sw.iot_web_backend.device.service;

import com.iot_sw.iot_web_backend.device.dto.request.RegisterRequestDTO;
import com.iot_sw.iot_web_backend.device.dto.request.TurnOffRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceMqttService {

    private final DeviceService deviceService;
    private final ObjectMapper objectMapper;

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
            else if (topic.startsWith("telemetry/")) {
                // 센서 데이터 처리 로직 구현 예정
            }
        } catch (Exception e) {
            log.error("MQTT 파싱/처리 중 오류: {}", e.getMessage());
        }
    }
}