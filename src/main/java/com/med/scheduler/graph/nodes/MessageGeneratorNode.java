package com.med.scheduler.graph.nodes;

import com.med.scheduler.graph.core.Node;
import com.med.scheduler.graph.state.ScheduleState;
import org.springframework.stereotype.Component;

/**
 * Nó do grafo responsável por produzir a resposta final humanizada adaptada ao idioma do paciente.
 */
@Component
public class MessageGeneratorNode implements Node<ScheduleState> {

    @Override
    public ScheduleState execute(ScheduleState state) {
        String result = (state.actionResult() != null) ? state.actionResult() : "Operação finalizada.";
        String language = (state.detectedLanguage() != null) ? state.detectedLanguage().toLowerCase() : "pt-br";

        String humanized;
        if (language.startsWith("en")) {
            humanized = state.actionSuccess()
                    ? "Hello! " + result + " We look forward to seeing you."
                    : "Attention: " + result;
        } else if (language.startsWith("es")) {
            humanized = state.actionSuccess()
                    ? "¡Hola! " + result + " Te esperamos."
                    : "Atención: " + result;
        } else {
            humanized = state.actionSuccess()
                    ? "Olá! " + result + " Aguardamos você."
                    : "Atenção: " + result;
        }

        return state.withHumanizedResponse(humanized);
    }
}
