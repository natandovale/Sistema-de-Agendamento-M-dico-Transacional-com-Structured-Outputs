package com.med.scheduler.graph;

import com.med.scheduler.graph.core.StateGraph;
import com.med.scheduler.graph.nodes.*;
import com.med.scheduler.graph.state.IntentType;
import com.med.scheduler.graph.state.ScheduleState;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Orquestrador central da Máquina de Estados (DAG).
 * Monta e executa o fluxo transacional completo de ponta a ponta.
 */
@Service
public class ScheduleGraphOrchestrator {

    private final StateGraph<ScheduleState> graph;

    public ScheduleGraphOrchestrator(
            IdentifyIntentNode identifyIntentNode,
            SchedulerNode schedulerNode,
            CancelNode cancelNode,
            FallbackNode fallbackNode,
            MessageGeneratorNode messageGeneratorNode) {
        Objects.requireNonNull(identifyIntentNode, "identifyIntentNode cannot be null");
        Objects.requireNonNull(schedulerNode, "schedulerNode cannot be null");
        Objects.requireNonNull(cancelNode, "cancelNode cannot be null");
        Objects.requireNonNull(fallbackNode, "fallbackNode cannot be null");
        Objects.requireNonNull(messageGeneratorNode, "messageGeneratorNode cannot be null");

        this.graph = new StateGraph<ScheduleState>("identifyIntent")
                .addNode("identifyIntent", identifyIntentNode)
                .addNode("scheduler", schedulerNode)
                .addNode("cancel", cancelNode)
                .addNode("fallback", fallbackNode)
                .addNode("messageGenerator", messageGeneratorNode)
                // Roteamento condicional a partir da intenção classificada
                .addConditionalEdge("identifyIntent", state -> {
                    if (state.intent() == IntentType.SCHEDULE) {
                        return "scheduler";
                    } else if (state.intent() == IntentType.CANCEL) {
                        return "cancel";
                    } else {
                        return "fallback";
                    }
                })
                // Todos os nós convergem deterministamente para o gerador de mensagem
                // humanizada
                .addEdge("scheduler", "messageGenerator")
                .addEdge("cancel", "messageGenerator")
                .addEdge("fallback", "messageGenerator")
                .addEdge("messageGenerator", StateGraph.END);
    }

    /**
     * Executa o processamento da mensagem do paciente através do grafo de estados.
     */
    public ScheduleState process(String conversationId, String patientId, String message) {
        ScheduleState initial = ScheduleState.initial(conversationId, patientId, message);

        return graph.execute(initial);
    }
}
