package com.iot_sw.iot_web_backend.device.controller;

import com.iot_sw.iot_web_backend.device.dto.request.ApproveRequestDTO;
import com.iot_sw.iot_web_backend.device.dto.request.DeviceConnectionUpdateRequestDTO;
import com.iot_sw.iot_web_backend.device.dto.request.RejectRequestDTO;
import com.iot_sw.iot_web_backend.device.dto.response.ApproveResponseDTO;
import com.iot_sw.iot_web_backend.device.dto.response.DeviceConnectionResponseDTO;
import com.iot_sw.iot_web_backend.device.service.DeviceService;
import com.iot_sw.iot_web_backend.device.enums.DeviceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/devices")
@CrossOrigin(origins = "*") // 리액트(5173 포트) 접속 허용
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    @GetMapping
    public List<DeviceConnectionResponseDTO> getAllDevices() {
        return deviceService.getAllDevices();
    }

    // 승인 대기 중(PENDING)인 기기 목록 조회
    @GetMapping("/pending")
    public List<DeviceConnectionResponseDTO> getPendingDevices() {
        return deviceService.getDevicesByStatus(DeviceStatus.PENDING);
    }

    @GetMapping("/online")
    public List<DeviceConnectionResponseDTO> getOnlineDevices() {
        return deviceService.getDevicesByStatus(DeviceStatus.ONLINE);
    }

    @PostMapping("/connection")
    public ResponseEntity<DeviceConnectionResponseDTO> upsertConnection(@RequestBody DeviceConnectionUpdateRequestDTO requestDTO) {
        DeviceConnectionResponseDTO responseDTO = deviceService.upsertConnection(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    // 기기 승인
    @PostMapping("/approve")
    public ResponseEntity<ApproveResponseDTO> approveDevice(@RequestBody ApproveRequestDTO requestDTO) {

        ApproveResponseDTO responseDTO = deviceService.approveDevice(requestDTO);
        return ResponseEntity.ok().body(responseDTO);
    }

    // 3. 기기 거절 처리 (상태를 REJECTED로 변경)
    @PostMapping("/reject")
    public ResponseEntity<?> rejectDevice(@RequestBody RejectRequestDTO requestDTO) {

        deviceService.rejectDevice(requestDTO);

        return ResponseEntity.ok().body("기기 거절 완료");
    }
}
