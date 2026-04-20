package com.guineajys.iot_web_backend.device.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// DTO 클래스
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ApproveRequestDTO {
    private String macId;
    private String name;
    private String location;
}
