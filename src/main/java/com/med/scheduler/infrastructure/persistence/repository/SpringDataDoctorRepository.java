package com.med.scheduler.infrastructure.persistence.repository;

import com.med.scheduler.infrastructure.persistence.entity.DoctorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataDoctorRepository extends JpaRepository<DoctorEntity, String> {

    Optional<DoctorEntity> findByNameIgnoreCase(String name);
}
