package com.guineajys.iot_web_backend.device.repository;

import com.guineajys.iot_web_backend.device.entity.Device;
import com.guineajys.iot_web_backend.device.enums.DeviceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List; // 💡 List 임포트 잊지 마세요!
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    Optional<Device> findByMacId(String macId);

    boolean existsByMacId(String macAddress);

    List<Device> findByStatus(DeviceStatus status);
}