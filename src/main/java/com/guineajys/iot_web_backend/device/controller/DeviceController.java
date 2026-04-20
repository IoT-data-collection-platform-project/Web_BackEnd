package com.guineajys.iot_web_backend.device.controller;

import com.guineajys.iot_web_backend.device.dto.request.ApproveRequestDTO;
import com.guineajys.iot_web_backend.device.dto.response.ApproveResponseDTO;
import com.guineajys.iot_web_backend.device.entity.Device;
import com.guineajys.iot_web_backend.device.repository.DeviceRepository;
import com.guineajys.iot_web_backend.device.service.DeviceService;
import com.guineajys.iot_web_backend.device.enums.DeviceStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/devices")
@CrossOrigin(origins = "*") // 리액트(5173 포트) 접속 허용
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;

    // 1. 승인 대기 중(PENDING)인 기기 목록 조회
    @GetMapping("/pending")
    public List<Device> getPendingDevices() {
        return deviceRepository.findByStatus(DeviceStatus.PENDING);
    }

    // 기기 승인
    @PostMapping("/approve")
    public ResponseEntity<ApproveResponseDTO> approveDevice(@RequestBody ApproveRequestDTO requestDTO) {

        ApproveResponseDTO responseDTO = deviceService.approveDevice(requestDTO);
        return ResponseEntity.ok().body(responseDTO);
    }

    // 3. 기기 거절 처리 (상태를 REJECTED로 변경하거나 삭제)
    @PostMapping("/reject")
    public ResponseEntity<?> rejectDevice(@RequestBody Map<String, String> request) {
        String macId = request.get("macId");

        Device device = deviceRepository.findByMacId(macId)
                .orElseThrow(() -> new RuntimeException("기기를 찾을 수 없습니다."));

        device.setStatus(DeviceStatus.REJECTED);
        deviceRepository.save(device);

        return ResponseEntity.ok().body("기기 거절 완료");
    }
}
