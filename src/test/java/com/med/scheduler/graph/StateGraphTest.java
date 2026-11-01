package com.med.scheduler.graph;

import com.med.scheduler.graph.core.Node;
import com.med.scheduler.graph.core.StateGraph;
import com.med.scheduler.graph.state.IntentType;
import com.med.scheduler.graph.state.ScheduleState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StateGraphTest {

    @Test
    @DisplayName("Deve executar nós em sequência linear até o nó END")
    void shouldExecuteLinearNodesUntilEnd() {
        // Arrange
        AtomicInteger counter = new AtomicInteger(0);

        Node<ScheduleState> step1 = state -> {
            counter.incrementAndGet();
            return state.withDetectedLanguage("pt-BR");
        };

        Node<ScheduleState> step2 = state -> {
            counter.incrementAndGet();
            return state.withHumanizedResponse("Processamento concluído em " + state.detectedLanguage());
        };

        StateGraph<ScheduleState> graph = new StateGraph<ScheduleState>("step1")
                .addNode("step1", step1)
                .addNode("step2", step2)
                .addEdge("step1", "step2")
                .addEdge("step2", StateGraph.END);

        ScheduleState initial = ScheduleState.initial("conv-1", "pat-1", "Quero marcar consulta");

        // Act
        ScheduleState result = graph.execute(initial);

        // Assert
        assertThat(counter.get()).isEqualTo(2);
        assertThat(result.detectedLanguage()).isEqualTo("pt-BR");
        assertThat(result.humanizedResponse()).isEqualTo("Processamento concluído em pt-BR");
    }

    @Test
    @DisplayName("Deve rotear condicionalmente para schedulerNode quando intent for SCHEDULE")
    void shouldRouteToSchedulerNodeWhenIntentIsSchedule() {
        // Arrange
        Node<ScheduleState> identifyIntent = state -> state.withIntent(IntentType.SCHEDULE);
        Node<ScheduleState> scheduler = state -> state.withActionResult("Consulta agendada com Dr. Silva", true);
        Node<ScheduleState> cancel = state -> state.withActionResult("Consulta cancelada", true);
        Node<ScheduleState> fallback = state -> state.withActionResult("Não compreendi", false);

        StateGraph<ScheduleState> graph = new StateGraph<ScheduleState>("identify")
                .addNode("identify", identifyIntent)
                .addNode("scheduler", scheduler)
                .addNode("cancel", cancel)
                .addNode("fallback", fallback)
                .addConditionalEdge("identify", state -> switch (state.intent()) {
                    case SCHEDULE -> "scheduler";
                    case CANCEL -> "cancel";
                    default -> "fallback";
                })
                .addEdge("scheduler", StateGraph.END)
                .addEdge("cancel", StateGraph.END)
                .addEdge("fallback", StateGraph.END);

        ScheduleState initial = ScheduleState.initial("conv-2", "pat-2", "Agendar amanhã às 10h");

        // Act
        ScheduleState result = graph.execute(initial);

        // Assert
        assertThat(result.intent()).isEqualTo(IntentType.SCHEDULE);
        assertThat(result.actionSuccess()).isTrue();
        assertThat(result.actionResult()).isEqualTo("Consulta agendada com Dr. Silva");
    }

    @Test
    @DisplayName("Deve rotear condicionalmente para cancelNode quando intent for CANCEL")
    void shouldRouteToCancelNodeWhenIntentIsCancel() {
        // Arrange
        Node<ScheduleState> identifyIntent = state -> state.withIntent(IntentType.CANCEL);
        Node<ScheduleState> scheduler = state -> state.withActionResult("Agendado", true);
        Node<ScheduleState> cancel = state -> state.withActionResult("Cancelamento efetuado com sucesso", true);

        StateGraph<ScheduleState> graph = new StateGraph<ScheduleState>("identify")
                .addNode("identify", identifyIntent)
                .addNode("scheduler", scheduler)
                .addNode("cancel", cancel)
                .addConditionalEdge("identify", state -> state.intent() == IntentType.CANCEL ? "cancel" : "scheduler")
                .addEdge("cancel", StateGraph.END)
                .addEdge("scheduler", StateGraph.END);

        ScheduleState initial = ScheduleState.initial("conv-3", "pat-3", "Cancelar minha consulta");

        // Act
        ScheduleState result = graph.execute(initial);

        // Assert
        assertThat(result.intent()).isEqualTo(IntentType.CANCEL);
        assertThat(result.actionSuccess()).isTrue();
        assertThat(result.actionResult()).isEqualTo("Cancelamento efetuado com sucesso");
    }

    @Test
    @DisplayName("Deve lançar exceção ao configurar nó inexistente ou transição órfã")
    void shouldThrowExceptionWhenNodeIsNotFound() {
        StateGraph<ScheduleState> graph = new StateGraph<ScheduleState>("node1")
                .addNode("node1", state -> state)
                .addEdge("node1", "unregisteredNode");

        ScheduleState initial = ScheduleState.initial("conv-4", "pat-4", "Oi");

        assertThatThrownBy(() -> graph.execute(initial))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("unregisteredNode");
    }
}
