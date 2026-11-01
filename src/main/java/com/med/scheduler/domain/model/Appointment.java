package com.med.scheduler.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidade de domínio representando uma Consulta Médica.
 */
public record Appointment(
        String id,
        Doctor doctor,
        String patientId,
        LocalDateTime dateTime,
        AppointmentStatus status
) {
    public Appointment {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(doctor, "doctor cannot be null");
        Objects.requireNonNull(patientId, "patientId cannot be null");
        Objects.requireNonNull(dateTime, "dateTime cannot be null");
        Objects.requireNonNull(status, "status cannot be null");
    }

    public Appointment cancel() {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Consulta já se encontra cancelada.");
        }
        return new Appointment(
                this.id,
                this.doctor,
                this.patientId,
                this.dateTime,
                AppointmentStatus.CANCELLED
        );
    }
}
