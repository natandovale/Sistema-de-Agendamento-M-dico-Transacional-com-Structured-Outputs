package com.med.scheduler.domain.repository;

import com.med.scheduler.domain.model.Appointment;
import com.med.scheduler.domain.model.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Porta de saída (repositório) para operações de persistência de Consultas.
 */
public interface AppointmentRepositoryPort {

    Optional<Appointment> findByIdAndPatientId(String id, String patientId);

    boolean existsByDoctorIdAndDateTimeAndStatus(String doctorId, LocalDateTime dateTime, AppointmentStatus status);

    Appointment save(Appointment appointment);
}
