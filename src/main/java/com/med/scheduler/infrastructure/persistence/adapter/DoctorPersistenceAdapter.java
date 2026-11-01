package com.med.scheduler.infrastructure.persistence.adapter;

import com.med.scheduler.domain.model.Doctor;
import com.med.scheduler.domain.repository.DoctorRepositoryPort;
import com.med.scheduler.infrastructure.persistence.entity.DoctorEntity;
import com.med.scheduler.infrastructure.persistence.repository.SpringDataDoctorRepository;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@Component
public class DoctorPersistenceAdapter implements DoctorRepositoryPort {

    private final SpringDataDoctorRepository repository;

    public DoctorPersistenceAdapter(SpringDataDoctorRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository cannot be null");
    }

    @Override
    public Optional<Doctor> findById(String id) {
        return repository.findById(id).map(DoctorEntity::toDomain);
    }

    @Override
    public Optional<Doctor> findByNameIgnoreCase(String name) {
        return repository.findByNameIgnoreCase(name).map(DoctorEntity::toDomain);
    }

    @Override
    public Doctor save(Doctor doctor) {
        DoctorEntity entity = DoctorEntity.fromDomain(doctor);
        DoctorEntity saved = repository.save(entity);
        return saved.toDomain();
    }
}
