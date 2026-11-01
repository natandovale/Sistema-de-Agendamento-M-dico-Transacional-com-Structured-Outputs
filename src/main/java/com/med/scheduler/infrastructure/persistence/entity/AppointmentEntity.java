package com.med.scheduler.infrastructure.persistence.entity;

import com.med.scheduler.domain.model.Appointment;
import com.med.scheduler.domain.model.AppointmentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointments")
public class AppointmentEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false)
    private DoctorEntity doctor;

    @Column(name = "patient_id", nullable = false)
    private String patientId;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private AppointmentStatus status;

    public AppointmentEntity() {
    }

    public AppointmentEntity(String id, DoctorEntity doctor, String patientId, LocalDateTime dateTime, AppointmentStatus status) {
        this.id = id;
        this.doctor = doctor;
        this.patientId = patientId;
        this.dateTime = dateTime;
        this.status = status;
    }

    public static AppointmentEntity fromDomain(Appointment appointment) {
        return new AppointmentEntity(
                appointment.id(),
                DoctorEntity.fromDomain(appointment.doctor()),
                appointment.patientId(),
                appointment.dateTime(),
                appointment.status()
        );
    }

    public Appointment toDomain() {
        return new Appointment(
                this.id,
                this.doctor.toDomain(),
                this.patientId,
                this.dateTime,
                this.status
        );
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DoctorEntity getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorEntity doctor) {
        this.doctor = doctor;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }
}
