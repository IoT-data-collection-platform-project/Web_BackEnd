package com.iot_sw.iot_web_backend.device.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceConnectionUpdateRequestDTO {
    private String macId;
    private String ipAddress;
    private String name;
    private Boolean online;
}
