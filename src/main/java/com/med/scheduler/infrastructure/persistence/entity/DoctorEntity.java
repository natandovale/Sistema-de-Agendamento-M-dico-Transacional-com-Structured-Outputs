package com.med.scheduler.infrastructure.persistence.entity;

import com.med.scheduler.domain.model.Doctor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "doctors")
public class DoctorEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "specialty", nullable = false)
    private String specialty;

    public DoctorEntity() {
    }

    public DoctorEntity(String id, String name, String specialty) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
    }

    public static DoctorEntity fromDomain(Doctor doctor) {
        return new DoctorEntity(doctor.id(), doctor.name(), doctor.specialty());
    }

    public Doctor toDomain() {
        return new Doctor(this.id, this.name, this.specialty);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }
}
