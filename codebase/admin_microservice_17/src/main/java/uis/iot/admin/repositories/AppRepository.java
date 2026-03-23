package uis.iot.admin.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import uis.iot.admin.models.App;

public interface AppRepository extends JpaRepository<App,Long>{

    List<App> findByUserId(Long userId);

    Long countByUserId(Long userId);
}
