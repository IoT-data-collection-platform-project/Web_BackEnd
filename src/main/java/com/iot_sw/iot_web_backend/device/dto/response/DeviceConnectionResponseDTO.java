package com.iot_sw.iot_web_backend.device.dto.response;

import com.iot_sw.iot_web_backend.device.entity.Device;
import com.iot_sw.iot_web_backend.device.enums.DeviceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceConnectionResponseDTO {
    private Long id;
    private String name;
    private String ipAddress;
    private String macId;
    private DeviceStatus status;
    private boolean online;

    public static DeviceConnectionResponseDTO from(Device device) {
        DeviceStatus status = device.getStatus();
        return DeviceConnectionResponseDTO.builder()
                .id(device.getId())
                .name(device.getName())
                .ipAddress(device.getIpAddress())
                .macId(device.getMacId())
                .status(status)
                .online(status == DeviceStatus.ONLINE)
                .build();
    }
}
