package com.smartuis.module.persistence.repository;

import com.smartuis.module.domain.entity.Device;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class DeviceRepository {
    private final IDeviceRepository deviceRepository;

    public DeviceRepository(IDeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    public Optional<Device> findDeviceByDeviceId(String deviceId) {
        return deviceRepository.findDeviceByDeviceId(deviceId);
    }
}

