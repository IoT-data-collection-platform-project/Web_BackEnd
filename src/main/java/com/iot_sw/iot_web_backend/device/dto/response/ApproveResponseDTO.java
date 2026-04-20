package com.iot_sw.iot_web_backend.device.dto.response;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApproveResponseDTO {
    private Long id;
    private String name;
    private String location;
    private String ipAddress;
    private String macId;
}
