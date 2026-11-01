package com.med.scheduler.graph.nodes;

import com.med.scheduler.domain.model.Appointment;
import com.med.scheduler.domain.service.AppointmentDomainService;
import com.med.scheduler.graph.core.Node;
import com.med.scheduler.graph.state.ScheduleState;
import com.med.scheduler.infrastructure.ai.dto.IntentExtractionResult;
import com.med.scheduler.infrastructure.ai.dto.ScheduleDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Nó do grafo responsável por executar a verificação e reserva determinística de consulta no domínio.
 */
@Component
public class SchedulerNode implements Node<ScheduleState> {

    private final AppointmentDomainService appointmentDomainService;

    public SchedulerNode(AppointmentDomainService appointmentDomainService) {
        this.appointmentDomainService = Objects.requireNonNull(appointmentDomainService, "appointmentDomainService cannot be null");
    }

    @Override
    public ScheduleState execute(ScheduleState state) {
        if (!(state.command() instanceof IntentExtractionResult extraction) || extraction.scheduleDetails() == null) {
            return state.withActionResult("Dados insuficientes para agendamento. Por favor, informe o médico, data e horário desejados.", false);
        }

        ScheduleDetails details = extraction.scheduleDetails();
        if (details.doctorName() == null || details.doctorName().isBlank()) {
            return state.withActionResult("Nome do médico não informado para o agendamento.", false);
        }

        LocalDate date = (details.date() != null) ? details.date() : LocalDate.now().plusDays(1);
        LocalTime time = (details.time() != null) ? details.time() : LocalTime.of(9, 0);
        LocalDateTime appointmentDateTime = LocalDateTime.of(date, time);

        try {
            Appointment booked = appointmentDomainService.schedule(
                    details.doctorName(),
                    state.patientId(),
                    appointmentDateTime
            );
            String message = String.format("Consulta confirmada com sucesso: %s em %s às %s (Especialidade: %s).",
                    booked.doctor().name(),
                    booked.dateTime().toLocalDate(),
                    booked.dateTime().toLocalTime(),
                    booked.doctor().specialty()
            );
            return state.withActionResult(message, true);
        } catch (Exception e) {
            return state.withActionResult(e.getMessage(), false);
        }
    }
}
