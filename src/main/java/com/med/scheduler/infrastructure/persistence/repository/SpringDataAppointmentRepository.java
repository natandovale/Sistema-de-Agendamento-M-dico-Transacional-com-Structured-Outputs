package com.med.scheduler.infrastructure.persistence.repository;

import com.med.scheduler.domain.model.AppointmentStatus;
import com.med.scheduler.infrastructure.persistence.entity.AppointmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface SpringDataAppointmentRepository extends JpaRepository<AppointmentEntity, String> {

    Optional<AppointmentEntity> findByIdAndPatientId(String id, String patientId);

    boolean existsByDoctorIdAndDateTimeAndStatus(String doctorId, LocalDateTime dateTime, AppointmentStatus status);
}
