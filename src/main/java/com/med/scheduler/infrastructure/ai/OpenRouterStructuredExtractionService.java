package com.med.scheduler.infrastructure.ai;

import com.med.scheduler.infrastructure.ai.dto.IntentExtractionResult;
import com.med.scheduler.infrastructure.ai.port.LlmExtractionPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Implementação do serviço de extração estruturada de intenção utilizando Spring AI ChatClient.
 * Conectado a modelos com suporte a Structured Outputs / JSON Schema via OpenRouter.
 */
@Service
public class OpenRouterStructuredExtractionService implements LlmExtractionPort {

    private static final Logger log = LoggerFactory.getLogger(OpenRouterStructuredExtractionService.class);

    private static final String SYSTEM_PROMPT = """
            Você é um tradutor determinístico de intenções para um sistema de agendamento médico.
            Seu único objetivo é traduzir a mensagem de texto livre do paciente em um contrato JSON rígido com base no schema fornecido.
            
            Regras obrigatórias:
            1. Classifique a intenção estritamente entre:
               - SCHEDULE: quando o paciente deseja agendar ou marcar uma nova consulta médica.
               - CANCEL: quando o paciente deseja desmarcar ou cancelar uma consulta existente.
               - UNKNOWN: para qualquer outra mensagem, saudações isoladas, dúvidas ou assuntos fora de escopo.
            2. Identifique o idioma do paciente (ex: 'pt-BR', 'en-US', 'es-ES').
            3. Para SCHEDULE, extraia doctorName, specialty (se mencionada), date (formato YYYY-MM-DD considerando a data atual como referência) e time (formato HH:mm).
            4. Para CANCEL, extraia appointmentId (se informado), doctorName e date.
            5. Nunca invente dados que o paciente não forneceu. Se a data ou horário for ambíguo, forneça o que puder e explique no campo reasoning.
            """;

    private final ChatClient chatClient;

    public OpenRouterStructuredExtractionService(ChatClient.Builder chatClientBuilder) {
        Objects.requireNonNull(chatClientBuilder, "chatClientBuilder cannot be null");
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public IntentExtractionResult extractIntentAndParameters(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return IntentExtractionResult.unknown("Mensagem vazia.");
        }

        try {
            IntentExtractionResult result = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(rawMessage)
                    .call()
                    .entity(IntentExtractionResult.class);

            return (result != null) ? result : IntentExtractionResult.unknown("Nenhum resultado retornado pelo modelo.");
        } catch (Exception e) {
            log.error("Falha ao processar Structured Output com LLM: {}", e.getMessage(), e);
            return IntentExtractionResult.unknown("Falha na interpretação da mensagem pelo modelo: " + e.getMessage());
        }
    }
}
