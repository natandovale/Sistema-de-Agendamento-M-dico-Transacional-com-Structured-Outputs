package com.med.scheduler.graph.nodes;

import com.med.scheduler.graph.core.Node;
import com.med.scheduler.graph.state.ScheduleState;
import com.med.scheduler.infrastructure.ai.dto.IntentExtractionResult;
import com.med.scheduler.infrastructure.ai.port.LlmExtractionPort;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Nó do grafo responsável por acionar o LLM e extrair a intenção e parâmetros estruturados.
 */
@Component
public class IdentifyIntentNode implements Node<ScheduleState> {

    private final LlmExtractionPort extractionPort;

    public IdentifyIntentNode(LlmExtractionPort extractionPort) {
        this.extractionPort = Objects.requireNonNull(extractionPort, "extractionPort cannot be null");
    }

    @Override
    public ScheduleState execute(ScheduleState state) {
        IntentExtractionResult extraction = extractionPort.extractIntentAndParameters(state.rawInputMessage());

        return state.withDetectedLanguage(extraction.language())
                .withIntent(extraction.intent())
                .withCommand(extraction);
    }
}
