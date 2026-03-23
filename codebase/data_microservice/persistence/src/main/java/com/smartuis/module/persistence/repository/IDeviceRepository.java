package com.smartuis.module.persistence.repository;

import com.smartuis.module.domain.entity.Device;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface IDeviceRepository extends MongoRepository<Device, String> {
    @Query("{'deviceId': ?0}")
    Optional<Device> findDeviceByDeviceId(String deviceId);
}

