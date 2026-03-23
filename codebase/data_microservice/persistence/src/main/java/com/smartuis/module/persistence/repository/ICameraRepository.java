package com.smartuis.module.persistence.repository;

import com.smartuis.module.domain.entity.Camera;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ICameraRepository extends MongoRepository<Camera, String> {

    boolean existsByName(String name);

    boolean existsByUrl(String url);
}
