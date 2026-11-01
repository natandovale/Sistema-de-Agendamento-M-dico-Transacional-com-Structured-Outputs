package com.med.scheduler.graph.state;

import java.util.Objects;

/**
 * Estado imutável compartilhado entre todos os nós da máquina de estados
 * (StateGraph).
 */
public record ScheduleState(
        String conversationId,
        String patientId,
        String rawInputMessage,
        String detectedLanguage,
        IntentType intent,
        Object command,
        String actionResult,
        boolean actionSuccess,
        String humanizedResponse) {

    public ScheduleState {
        Objects.requireNonNull(conversationId, "conversationId cannot be null");
        Objects.requireNonNull(patientId, "patientId cannot be null");
        Objects.requireNonNull(rawInputMessage, "rawInputMessage cannot be null");
    }

    public static ScheduleState initial(String conversationId, String patientId, String rawInputMessage) {
        return new ScheduleState(
                conversationId,
                patientId,
                rawInputMessage,
                null,
                IntentType.UNKNOWN,
                null,
                null,
                false,
                null);
    }

    public ScheduleState withDetectedLanguage(String detectedLanguage) {
        return new ScheduleState(
                this.conversationId,
                this.patientId,
                this.rawInputMessage,
                detectedLanguage,
                this.intent,
                this.command,
                this.actionResult,
                this.actionSuccess,
                this.humanizedResponse);
    }

    public ScheduleState withIntent(IntentType intent) {
        return new ScheduleState(
                this.conversationId,
                this.patientId,
                this.rawInputMessage,
                this.detectedLanguage,
                intent,
                this.command,
                this.actionResult,
                this.actionSuccess,
                this.humanizedResponse);
    }

    public ScheduleState withCommand(Object command) {
        return new ScheduleState(
                this.conversationId,
                this.patientId,
                this.rawInputMessage,
                this.detectedLanguage,
                this.intent,
                command,
                this.actionResult,
                this.actionSuccess,
                this.humanizedResponse);
    }

    public ScheduleState withActionResult(String actionResult, boolean actionSuccess) {
        return new ScheduleState(
                this.conversationId,
                this.patientId,
                this.rawInputMessage,
                this.detectedLanguage,
                this.intent,
                this.command,
                actionResult,
                actionSuccess,
                this.humanizedResponse);
    }

    public ScheduleState withHumanizedResponse(String humanizedResponse) {
        return new ScheduleState(
                this.conversationId,
                this.patientId,
                this.rawInputMessage,
                this.detectedLanguage,
                this.intent,
                this.command,
                this.actionResult,
                this.actionSuccess,
                humanizedResponse);
    }
}
