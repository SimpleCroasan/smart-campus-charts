package uis.iot.admin.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uis.iot.admin.models.DeviceModel;

public interface DeviceModelRepository extends JpaRepository<DeviceModel, Long> {

    List<DeviceModel> findByUserId(Long userId);

    Long countByUserId(Long userId);
        
}
