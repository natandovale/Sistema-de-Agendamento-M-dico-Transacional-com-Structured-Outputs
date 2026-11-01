package com.med.scheduler.infrastructure.ai.dto;

import com.med.scheduler.graph.state.IntentType;

import java.util.Objects;

/**
 * Contrato de saída estruturada (Structured Output) gerado pelo LLM.
 */
public record IntentExtractionResult(
        IntentType intent,
        String language,
        ScheduleDetails scheduleDetails,
        CancelDetails cancelDetails,
        String reasoning
) {
    public IntentExtractionResult {
        intent = (intent != null) ? intent : IntentType.UNKNOWN;
        language = (language != null && !language.isBlank()) ? language : "pt-BR";
    }

    public static IntentExtractionResult unknown(String reason) {
        return new IntentExtractionResult(
                IntentType.UNKNOWN,
                "pt-BR",
                null,
                null,
                reason
        );
    }
}
