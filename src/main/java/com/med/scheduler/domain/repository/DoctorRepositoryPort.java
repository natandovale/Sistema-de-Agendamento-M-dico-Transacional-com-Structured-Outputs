package com.med.scheduler.domain.repository;

import com.med.scheduler.domain.model.Doctor;

import java.util.Optional;

/**
 * Porta de saída (repositório) para operações de leitura/escrita de Médicos.
 */
public interface DoctorRepositoryPort {

    Optional<Doctor> findById(String id);

    Optional<Doctor> findByNameIgnoreCase(String name);

    Doctor save(Doctor doctor);
}
