package com.med.scheduler.infrastructure.web;

import com.med.scheduler.graph.ScheduleGraphOrchestrator;
import com.med.scheduler.graph.state.ScheduleState;
import com.med.scheduler.infrastructure.web.dto.ChatScheduleRequest;
import com.med.scheduler.infrastructure.web.dto.ChatScheduleResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Controller REST expondo o endpoint corporativo de agendamento conversacional.
 */
@RestController
@RequestMapping("/api/v1/chat")
public class ChatScheduleController {

    private final ScheduleGraphOrchestrator orchestrator;

    public ChatScheduleController(ScheduleGraphOrchestrator orchestrator) {
        this.orchestrator = Objects.requireNonNull(orchestrator, "orchestrator cannot be null");
    }

    @PostMapping("/schedule")
    public ResponseEntity<ChatScheduleResponse> processMessage(@Valid @RequestBody ChatScheduleRequest request) {
        ScheduleState finalState = orchestrator.process(
                request.conversationId(),
                request.patientId(),
                request.message()
        );

        return ResponseEntity.ok(ChatScheduleResponse.fromState(finalState));
    }
}
