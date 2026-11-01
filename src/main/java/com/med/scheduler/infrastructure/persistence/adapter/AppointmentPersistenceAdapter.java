package com.med.scheduler.infrastructure.persistence.adapter;

import com.med.scheduler.domain.model.Appointment;
import com.med.scheduler.domain.model.AppointmentStatus;
import com.med.scheduler.domain.repository.AppointmentRepositoryPort;
import com.med.scheduler.infrastructure.persistence.entity.AppointmentEntity;
import com.med.scheduler.infrastructure.persistence.repository.SpringDataAppointmentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Component
public class AppointmentPersistenceAdapter implements AppointmentRepositoryPort {

    private final SpringDataAppointmentRepository repository;

    public AppointmentPersistenceAdapter(SpringDataAppointmentRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository cannot be null");
    }

    @Override
    public Optional<Appointment> findByIdAndPatientId(String id, String patientId) {
        return repository.findByIdAndPatientId(id, patientId).map(AppointmentEntity::toDomain);
    }

    @Override
    public boolean existsByDoctorIdAndDateTimeAndStatus(String doctorId, LocalDateTime dateTime, AppointmentStatus status) {
        return repository.existsByDoctorIdAndDateTimeAndStatus(doctorId, dateTime, status);
    }

    @Override
    public Appointment save(Appointment appointment) {
        AppointmentEntity entity = AppointmentEntity.fromDomain(appointment);
        AppointmentEntity saved = repository.save(entity);
        return saved.toDomain();
    }
}
