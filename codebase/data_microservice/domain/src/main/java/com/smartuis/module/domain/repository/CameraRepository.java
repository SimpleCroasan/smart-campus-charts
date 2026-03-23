package com.smartuis.module.domain.repository;

import com.smartuis.module.domain.entity.Camera;

import java.util.List;

public interface CameraRepository {

    Camera findById(String idCamera);

    Camera save(Camera camera);

    Camera delete(Camera camera);

    List<Camera> findAll();

    List<Camera> saveAll(List<Camera> cameras);

    boolean existsByName(String name);

    boolean existsByUrl(String url);
}
