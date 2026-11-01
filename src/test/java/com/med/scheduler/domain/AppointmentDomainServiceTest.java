package com.med.scheduler.domain;

import com.med.scheduler.domain.model.Appointment;
import com.med.scheduler.domain.model.AppointmentStatus;
import com.med.scheduler.domain.model.Doctor;
import com.med.scheduler.domain.repository.AppointmentRepositoryPort;
import com.med.scheduler.domain.repository.DoctorRepositoryPort;
import com.med.scheduler.domain.service.AppointmentDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentDomainServiceTest {

    @Mock
    private DoctorRepositoryPort doctorRepository;

    @Mock
    private AppointmentRepositoryPort appointmentRepository;

    private AppointmentDomainService appointmentService;

    private Doctor doctor;
    private LocalDateTime appointmentTime;

    @BeforeEach
    void setUp() {
        appointmentService = new AppointmentDomainService(doctorRepository, appointmentRepository);
        doctor = new Doctor("doc-1", "Dr. Alício", "Cardiologia");
        appointmentTime = LocalDateTime.of(2026, 11, 10, 14, 0);
    }

    @Test
    @DisplayName("Deve agendar consulta com sucesso quando médico existe e horário está livre")
    void shouldScheduleAppointmentSuccessfully() {
        // Arrange
        when(doctorRepository.findByNameIgnoreCase("Dr. Alício")).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsByDoctorIdAndDateTimeAndStatus(
                doctor.id(), appointmentTime, AppointmentStatus.CONFIRMED)).thenReturn(false);

        Appointment savedAppointment = new Appointment("app-1", doctor, "pat-123", appointmentTime, AppointmentStatus.CONFIRMED);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        // Act
        Appointment result = appointmentService.schedule("Dr. Alício", "pat-123", appointmentTime);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("app-1");
        assertThat(result.status()).isEqualTo(AppointmentStatus.CONFIRMED);
        assertThat(result.doctor().name()).isEqualTo("Dr. Alício");
        assertThat(result.patientId()).isEqualTo("pat-123");
        assertThat(result.dateTime()).isEqualTo(appointmentTime);

        verify(appointmentRepository).save(any(Appointment.class));
    }

    @Test
    @DisplayName("Deve falhar ao tentar agendar consulta quando médico não for encontrado")
    void shouldFailWhenDoctorNotFound() {
        // Arrange
        when(doctorRepository.findByNameIgnoreCase("Dr. Desconhecido")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.schedule("Dr. Desconhecido", "pat-123", appointmentTime))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Médico 'Dr. Desconhecido' não encontrado.");
    }

    @Test
    @DisplayName("Deve falhar com conflito de horário quando médico já possui agendamento confirmado naquele horário")
    void shouldFailWhenSlotIsAlreadyBooked() {
        // Arrange
        when(doctorRepository.findByNameIgnoreCase("Dr. Alício")).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsByDoctorIdAndDateTimeAndStatus(
                doctor.id(), appointmentTime, AppointmentStatus.CONFIRMED)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.schedule("Dr. Alício", "pat-123", appointmentTime))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Horário indisponível para o médico Dr. Alício");
    }

    @Test
    @DisplayName("Deve cancelar consulta ativa com sucesso")
    void shouldCancelAppointmentSuccessfully() {
        // Arrange
        Appointment activeAppointment = new Appointment("app-1", doctor, "pat-123", appointmentTime, AppointmentStatus.CONFIRMED);
        when(appointmentRepository.findByIdAndPatientId("app-1", "pat-123")).thenReturn(Optional.of(activeAppointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Appointment cancelled = appointmentService.cancel("app-1", "pat-123");

        // Assert
        assertThat(cancelled.status()).isEqualTo(AppointmentStatus.CANCELLED);
        verify(appointmentRepository).save(cancelled);
    }

    @Test
    @DisplayName("Deve falhar ao tentar cancelar consulta inexistente")
    void shouldFailWhenCancellingNonExistentAppointment() {
        // Arrange
        when(appointmentRepository.findByIdAndPatientId("app-999", "pat-123")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> appointmentService.cancel("app-999", "pat-123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Consulta 'app-999' não encontrada para o paciente informado.");
    }
}
