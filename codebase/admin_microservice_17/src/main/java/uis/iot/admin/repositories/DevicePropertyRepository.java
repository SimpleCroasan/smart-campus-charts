package uis.iot.admin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import uis.iot.admin.models.DeviceProperty;

public interface DevicePropertyRepository extends JpaRepository<DeviceProperty, Long> {
    
}
