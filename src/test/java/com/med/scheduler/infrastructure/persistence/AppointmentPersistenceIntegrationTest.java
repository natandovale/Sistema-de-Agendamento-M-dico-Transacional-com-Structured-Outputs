package com.med.scheduler.infrastructure.persistence;

import com.med.scheduler.domain.model.Appointment;
import com.med.scheduler.domain.model.AppointmentStatus;
import com.med.scheduler.domain.model.Doctor;
import com.med.scheduler.domain.repository.AppointmentRepositoryPort;
import com.med.scheduler.domain.repository.DoctorRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AppointmentPersistenceIntegrationTest {

    @Autowired
    private DoctorRepositoryPort doctorRepository;

    @Autowired
    private AppointmentRepositoryPort appointmentRepository;

    @Test
    @DisplayName("Deve persistir e recuperar médico por nome ignorando maiúsculas/minúsculas")
    void shouldPersistAndFindDoctorByNameIgnoreCase() {
        Doctor doctor = new Doctor("doc-persist-1", "Dra. Beatriz Santos", "Dermatologia");
        doctorRepository.save(doctor);

        Optional<Doctor> found = doctorRepository.findByNameIgnoreCase("dra. beatriz santos");

        assertThat(found).isPresent();
        assertThat(found.get().id()).isEqualTo("doc-persist-1");
        assertThat(found.get().name()).isEqualTo("Dra. Beatriz Santos");
        assertThat(found.get().specialty()).isEqualTo("Dermatologia");
    }

    @Test
    @DisplayName("Deve persistir agendamento e verificar existência de conflito de horário")
    void shouldPersistAppointmentAndDetectConflict() {
        Doctor doctor = new Doctor("doc-persist-2", "Dr. Carlos Eduardo", "Ortopedia");
        doctorRepository.save(doctor);

        LocalDateTime slot = LocalDateTime.of(2026, 11, 15, 9, 30);
        Appointment appointment = new Appointment("app-persist-1", doctor, "pat-999", slot, AppointmentStatus.CONFIRMED);
        appointmentRepository.save(appointment);

        boolean existsConfirmed = appointmentRepository.existsByDoctorIdAndDateTimeAndStatus(
                doctor.id(), slot, AppointmentStatus.CONFIRMED
        );
        boolean existsCancelled = appointmentRepository.existsByDoctorIdAndDateTimeAndStatus(
                doctor.id(), slot, AppointmentStatus.CANCELLED
        );

        assertThat(existsConfirmed).isTrue();
        assertThat(existsCancelled).isFalse();

        Optional<Appointment> retrieved = appointmentRepository.findByIdAndPatientId("app-persist-1", "pat-999");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().doctor().name()).isEqualTo("Dr. Carlos Eduardo");
    }
}
