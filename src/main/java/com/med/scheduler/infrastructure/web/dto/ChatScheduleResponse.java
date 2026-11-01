package com.med.scheduler.infrastructure.web.dto;

import com.med.scheduler.graph.state.IntentType;
import com.med.scheduler.graph.state.ScheduleState;

/**
 * Payload de resposta retornado pela API REST.
 */
public record ChatScheduleResponse(
        String conversationId,
        String patientId,
        IntentType intent,
        boolean actionSuccess,
        String actionResult,
        String humanizedResponse
) {
    public static ChatScheduleResponse fromState(ScheduleState state) {
        return new ChatScheduleResponse(
                state.conversationId(),
                state.patientId(),
                state.intent(),
                state.actionSuccess(),
                state.actionResult(),
                state.humanizedResponse()
        );
    }
}
