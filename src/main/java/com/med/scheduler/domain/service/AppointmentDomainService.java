package com.med.scheduler.domain.service;

import com.med.scheduler.domain.model.Appointment;
import com.med.scheduler.domain.model.AppointmentStatus;
import com.med.scheduler.domain.model.Doctor;
import com.med.scheduler.domain.repository.AppointmentRepositoryPort;
import com.med.scheduler.domain.repository.DoctorRepositoryPort;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Serviço de domínio determinístico para regras de agendamento e cancelamento.
 * Código puramente isolado de frameworks e bibliotecas externas.
 */
public class AppointmentDomainService {

    private final DoctorRepositoryPort doctorRepository;
    private final AppointmentRepositoryPort appointmentRepository;

    public AppointmentDomainService(DoctorRepositoryPort doctorRepository, AppointmentRepositoryPort appointmentRepository) {
        this.doctorRepository = Objects.requireNonNull(doctorRepository, "doctorRepository cannot be null");
        this.appointmentRepository = Objects.requireNonNull(appointmentRepository, "appointmentRepository cannot be null");
    }

    /**
     * Agenda uma nova consulta médica de forma determinística após validação de médico e disponibilidade.
     */
    public Appointment schedule(String doctorName, String patientId, LocalDateTime dateTime) {
        Objects.requireNonNull(doctorName, "doctorName cannot be null");
        Objects.requireNonNull(patientId, "patientId cannot be null");
        Objects.requireNonNull(dateTime, "dateTime cannot be null");

        Doctor doctor = doctorRepository.findByNameIgnoreCase(doctorName.trim())
                .orElseThrow(() -> new IllegalArgumentException("Médico '" + doctorName + "' não encontrado."));

        boolean alreadyBooked = appointmentRepository.existsByDoctorIdAndDateTimeAndStatus(
                doctor.id(), dateTime, AppointmentStatus.CONFIRMED
        );

        if (alreadyBooked) {
            throw new IllegalStateException("Horário indisponível para o médico " + doctor.name() + " na data e horário solicitados.");
        }

        Appointment newAppointment = new Appointment(
                UUID.randomUUID().toString(),
                doctor,
                patientId,
                dateTime,
                AppointmentStatus.CONFIRMED
        );

        return appointmentRepository.save(newAppointment);
    }

    /**
     * Cancela uma consulta existente com liberação de horário.
     */
    public Appointment cancel(String appointmentId, String patientId) {
        Objects.requireNonNull(appointmentId, "appointmentId cannot be null");
        Objects.requireNonNull(patientId, "patientId cannot be null");

        Appointment appointment = appointmentRepository.findByIdAndPatientId(appointmentId, patientId)
                .orElseThrow(() -> new IllegalArgumentException("Consulta '" + appointmentId + "' não encontrada para o paciente informado."));

        Appointment cancelledAppointment = appointment.cancel();
        return appointmentRepository.save(cancelledAppointment);
    }
}
