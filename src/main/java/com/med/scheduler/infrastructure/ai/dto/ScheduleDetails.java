package com.med.scheduler.infrastructure.ai.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Contrato estruturado com os detalhes para agendamento de consulta extraídos pelo LLM.
 */
public record ScheduleDetails(
        String doctorName,
        String specialty,
        LocalDate date,
        LocalTime time
) {}
