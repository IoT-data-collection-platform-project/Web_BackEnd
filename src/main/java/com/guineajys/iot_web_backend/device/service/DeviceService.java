package com.guineajys.iot_web_backend.device.service;

import com.guineajys.iot_web_backend.device.dto.request.ApproveRequestDTO;
import com.guineajys.iot_web_backend.device.dto.request.RegisterRequestDTO;
import com.guineajys.iot_web_backend.device.dto.response.ApproveResponseDTO;
import com.guineajys.iot_web_backend.device.repository.DeviceRepository;
import com.guineajys.iot_web_backend.device.entity.Device;
import com.guineajys.iot_web_backend.device.enums.DeviceStatus;
import com.guineajys.iot_web_backend.mqtt.MqttGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final MqttGateway mqttGateway;

    // 📡 1. 신규 기기 등록 (MqttService에서 호출됨)
    @Transactional
    public void registerPendingDevice(RegisterRequestDTO requestDTO) {

        // 1-1. DB에 아예 없는 완전 신규 기기인 경우
        if (!deviceRepository.existsByMacId(requestDTO.getMacId())) {
            Device newDevice = new Device();
            newDevice.setMacId(requestDTO.getMacId());
            newDevice.setIpAddress(requestDTO.getIpAddress());

            // 여기서 명시적으로 PENDING으로 덮어씌웁니다! (관리자 승인 대기열로 이동)
            newDevice.setStatus(DeviceStatus.PENDING);

            deviceRepository.save(newDevice);
            log.info("새 기기 연결 요청 (승인 대기): {}", requestDTO.getMacId());
        }
        // 1-2. 이미 DB에 있는 기기인 경우 (정전 등으로 재부팅되어 다시 요청을 보냄)
        else {
            Device existingDevice = deviceRepository.findByMacId(requestDTO.getMacId()).get();

            if (existingDevice.getStatus() == DeviceStatus.ONLINE || existingDevice.getStatus() == DeviceStatus.OFFLINE) {
                log.info("이미 승인된 기기의 재접속 요청입니다. 허가증을 재발급합니다: {}", requestDTO.getMacId());

                // 다시 ONLINE 상태로 갱신
                existingDevice.setStatus(DeviceStatus.ONLINE);
                existingDevice.setIpAddress(requestDTO.getIpAddress()); // IP가 바뀌었을 수 있으니 갱신
                deviceRepository.save(existingDevice);

                // 관리자 승인 없이 즉시 허가증(Response)을 파이로 다시 쏴줍니다!
                String topic = "provisioning/response/" + requestDTO.getMacId();
                String approvalMsg = String.format(
                        "{\"status\": \"APPROVED\", \"device_id\": %d, \"name\": \"%s\"}",
                        existingDevice.getId(), existingDevice.getName()
                );
                mqttGateway.sendToMqtt(approvalMsg, topic);
            } else if (existingDevice.getStatus() == DeviceStatus.PENDING) {
                log.info("⏳ 아직 관리자가 승인하지 않은 기기입니다. 대기 중: {}", requestDTO.getMacId());
            }
        }
    }

    // 2. 기기 승인 및 허가증 발급 (React 웹의 Controller에서 호출될 예정)
    @Transactional
    public ApproveResponseDTO approveDevice(ApproveRequestDTO requestDTO) {
        Device device = deviceRepository.findByMacId(requestDTO.getMacId())
                .orElseThrow(() -> new RuntimeException("기기를 찾을 수 없습니다."));

        // 상태 변경 및 이름 부여 (엔티티의 메서드 활용)
        device.approve(requestDTO.getName(), requestDTO.getLocation());
        Device savedDevice = deviceRepository.save(device);

        // 승인 메시지 조립 및 전송
        String topic = "provisioning/response/" + requestDTO.getMacId();
        String approvalMsg = String.format(
                "{\"status\": \"APPROVED\", \"device_id\": %d, \"name\": \"%s\"}",
                device.getId(), requestDTO.getName()
        );

        mqttGateway.sendToMqtt(approvalMsg, topic);
        log.info("기기 승인 완료 및 허가증 발급: {}", requestDTO.getMacId());

        return ApproveResponseDTO.builder()
                .id(savedDevice.getId())
                .name(savedDevice.getName())
                .location(savedDevice.getLocation())
                .ipAddress(savedDevice.getIpAddress())
                .macId(savedDevice.getMacId())
                .build();
    }
}