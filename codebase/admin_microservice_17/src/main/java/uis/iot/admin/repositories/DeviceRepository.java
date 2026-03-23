package uis.iot.admin.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uis.iot.admin.models.Device;

public interface DeviceRepository extends JpaRepository<Device, Long> {

    List<Device> findByUserId(Long userId);

    Long countByUserId(Long userId);
    
}
