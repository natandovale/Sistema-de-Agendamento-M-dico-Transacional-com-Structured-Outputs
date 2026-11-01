package com.med.scheduler.infrastructure.ai.dto;

import java.time.LocalDate;

/**
 * Contrato estruturado com os detalhes para cancelamento de consulta extraídos pelo LLM.
 */
public record CancelDetails(
        String appointmentId,
        String doctorName,
        LocalDate date
) {}
