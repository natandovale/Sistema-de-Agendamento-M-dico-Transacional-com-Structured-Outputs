package com.med.scheduler.graph.nodes;

import com.med.scheduler.graph.core.Node;
import com.med.scheduler.graph.state.ScheduleState;
import org.springframework.stereotype.Component;

/**
 * Nó do grafo acionado quando a intenção não pôde ser identificada ou a solicitação está fora de escopo.
 */
@Component
public class FallbackNode implements Node<ScheduleState> {

    @Override
    public ScheduleState execute(ScheduleState state) {
        String guidance = "Não compreendi sua solicitação. Posso ajudar você a agendar ou cancelar consultas médicas. " +
                "Exemplo: 'Gostaria de agendar consulta com Dr. Alício amanhã às 14h' ou 'Desejo cancelar minha consulta'.";

        return state.withActionResult(guidance, false);
    }
}
