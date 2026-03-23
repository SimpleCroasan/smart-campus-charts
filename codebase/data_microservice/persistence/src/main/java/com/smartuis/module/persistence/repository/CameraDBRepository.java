package com.smartuis.module.persistence.repository;

import com.smartuis.module.domain.entity.Camera;
import com.smartuis.module.domain.repository.CameraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CameraDBRepository implements CameraRepository {

    private ICameraRepository iCameraRepository;

    public CameraDBRepository(ICameraRepository iCameraRepository) {
        this.iCameraRepository = iCameraRepository;
    }

    @Override
    public Camera findById(String idCamera) {
        return iCameraRepository.findById(idCamera).orElse(null);
    }

    @Override
    public Camera save(Camera camera) {
        return iCameraRepository.save(camera);
    }

    @Override
    public Camera delete(Camera camera) {
        iCameraRepository.delete(camera);
        return camera;
    }

    @Override
    public List<Camera> findAll() {
        return iCameraRepository.findAll();
    }

    @Override
    public List<Camera> saveAll(List<Camera> cameras) {
        return iCameraRepository.saveAll(cameras);
    }

    @Override
    public boolean existsByName(String namera) {
        return iCameraRepository.existsByName(namera);
    }

    @Override
    public boolean existsByUrl(String url) {
        return iCameraRepository.existsByUrl(url);
    }
}
