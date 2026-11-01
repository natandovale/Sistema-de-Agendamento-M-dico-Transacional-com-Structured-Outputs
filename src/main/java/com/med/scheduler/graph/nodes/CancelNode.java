package com.med.scheduler.graph.nodes;

import com.med.scheduler.domain.model.Appointment;
import com.med.scheduler.domain.service.AppointmentDomainService;
import com.med.scheduler.graph.core.Node;
import com.med.scheduler.graph.state.ScheduleState;
import com.med.scheduler.infrastructure.ai.dto.CancelDetails;
import com.med.scheduler.infrastructure.ai.dto.IntentExtractionResult;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Nó do grafo responsável por executar o cancelamento determinístico no domínio.
 */
@Component
public class CancelNode implements Node<ScheduleState> {

    private final AppointmentDomainService appointmentDomainService;

    public CancelNode(AppointmentDomainService appointmentDomainService) {
        this.appointmentDomainService = Objects.requireNonNull(appointmentDomainService, "appointmentDomainService cannot be null");
    }

    @Override
    public ScheduleState execute(ScheduleState state) {
        if (!(state.command() instanceof IntentExtractionResult extraction) || extraction.cancelDetails() == null) {
            return state.withActionResult("Dados insuficientes para cancelamento. Por favor, informe o identificador da consulta.", false);
        }

        CancelDetails details = extraction.cancelDetails();
        String appointmentId = details.appointmentId();
        if (appointmentId == null || appointmentId.isBlank()) {
            return state.withActionResult("Identificador da consulta não fornecido para cancelamento.", false);
        }

        try {
            Appointment cancelled = appointmentDomainService.cancel(appointmentId, state.patientId());
            String message = String.format("Consulta cancelada com sucesso para o paciente %s.", state.patientId());
            return state.withActionResult(message, true);
        } catch (Exception e) {
            return state.withActionResult(e.getMessage(), false);
        }
    }
}
