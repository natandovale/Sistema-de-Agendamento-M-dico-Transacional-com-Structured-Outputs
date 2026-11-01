package com.med.scheduler.infrastructure.ai;

import com.med.scheduler.graph.state.IntentType;
import com.med.scheduler.infrastructure.ai.dto.CancelDetails;
import com.med.scheduler.infrastructure.ai.dto.IntentExtractionResult;
import com.med.scheduler.infrastructure.ai.dto.ScheduleDetails;
import com.med.scheduler.infrastructure.ai.port.LlmExtractionPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmStructuredExtractionServiceTest {

    @Mock
    private ChatClient.Builder chatClientBuilder;

    @Mock
    private ChatClient chatClient;

    private LlmExtractionPort extractionService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        extractionService = new OpenRouterStructuredExtractionService(chatClientBuilder);
    }

    @Test
    @DisplayName("Deve extrair com sucesso intenção de agendamento com dados estruturados")
    void shouldExtractScheduleIntentSuccessfully() {
        // Arrange
        String patientMessage = "Quero marcar consulta com o Dr. Alício amanhã às 14h";

        ScheduleDetails scheduleDetails = new ScheduleDetails(
                "Dr. Alício",
                "Cardiologia",
                LocalDate.now().plusDays(1),
                LocalTime.of(14, 0)
        );

        IntentExtractionResult expectedResult = new IntentExtractionResult(
                IntentType.SCHEDULE,
                "pt-BR",
                scheduleDetails,
                null,
                "Paciente deseja agendamento de consulta com Dr. Alício para amanhã às 14h."
        );

        setupMockChatClientResponse(expectedResult);

        // Act
        IntentExtractionResult result = extractionService.extractIntentAndParameters(patientMessage);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.intent()).isEqualTo(IntentType.SCHEDULE);
        assertThat(result.language()).isEqualTo("pt-BR");
        assertThat(result.scheduleDetails()).isNotNull();
        assertThat(result.scheduleDetails().doctorName()).isEqualTo("Dr. Alício");
        assertThat(result.scheduleDetails().time()).isEqualTo(LocalTime.of(14, 0));
        assertThat(result.cancelDetails()).isNull();
    }

    @Test
    @DisplayName("Deve extrair com sucesso intenção de cancelamento")
    void shouldExtractCancelIntentSuccessfully() {
        // Arrange
        String patientMessage = "Preciso cancelar minha consulta com Dr. Carlos de quinta-feira";

        CancelDetails cancelDetails = new CancelDetails(
                null,
                "Dr. Carlos",
                LocalDate.now().plusDays(2)
        );

        IntentExtractionResult expectedResult = new IntentExtractionResult(
                IntentType.CANCEL,
                "pt-BR",
                null,
                cancelDetails,
                "Paciente deseja cancelar consulta agendada com Dr. Carlos."
        );

        setupMockChatClientResponse(expectedResult);

        // Act
        IntentExtractionResult result = extractionService.extractIntentAndParameters(patientMessage);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.intent()).isEqualTo(IntentType.CANCEL);
        assertThat(result.cancelDetails()).isNotNull();
        assertThat(result.cancelDetails().doctorName()).isEqualTo("Dr. Carlos");
        assertThat(result.scheduleDetails()).isNull();
    }

    @Test
    @DisplayName("Deve classificar como UNKNOWN para mensagens sem intenção médica clara")
    void shouldExtractUnknownForUnclearMessage() {
        // Arrange
        String patientMessage = "Qual é a previsão do tempo para amanhã?";

        IntentExtractionResult expectedResult = new IntentExtractionResult(
                IntentType.UNKNOWN,
                "pt-BR",
                null,
                null,
                "Mensagem fora do escopo de agendamento ou cancelamento médico."
        );

        setupMockChatClientResponse(expectedResult);

        // Act
        IntentExtractionResult result = extractionService.extractIntentAndParameters(patientMessage);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.intent()).isEqualTo(IntentType.UNKNOWN);
        assertThat(result.scheduleDetails()).isNull();
        assertThat(result.cancelDetails()).isNull();
    }

    @SuppressWarnings("unchecked")
    private void setupMockChatClientResponse(IntentExtractionResult expectedResult) {
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec callResponseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(any(Class.class))).thenReturn(expectedResult);
    }
}
