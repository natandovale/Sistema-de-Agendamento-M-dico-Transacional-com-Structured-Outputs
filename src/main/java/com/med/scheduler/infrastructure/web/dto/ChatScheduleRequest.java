package com.med.scheduler.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload de entrada para processamento de mensagem do paciente.
 */
public record ChatScheduleRequest(
        @NotBlank(message = "conversationId é obrigatório")
        String conversationId,

        @NotBlank(message = "patientId é obrigatório")
        String patientId,

        @NotBlank(message = "message é obrigatória")
        String message
) {}
