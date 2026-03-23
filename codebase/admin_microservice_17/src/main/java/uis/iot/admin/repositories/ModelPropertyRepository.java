package uis.iot.admin.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import uis.iot.admin.models.ModelProperty;

public interface ModelPropertyRepository extends JpaRepository<ModelProperty, Long> {
  
}
