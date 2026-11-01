package com.med.scheduler.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.med.scheduler.domain.model.Doctor;
import com.med.scheduler.domain.repository.DoctorRepositoryPort;
import com.med.scheduler.graph.state.IntentType;
import com.med.scheduler.infrastructure.ai.dto.IntentExtractionResult;
import com.med.scheduler.infrastructure.ai.dto.ScheduleDetails;
import com.med.scheduler.infrastructure.ai.port.LlmExtractionPort;
import com.med.scheduler.infrastructure.web.dto.ChatScheduleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ChatScheduleControllerE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorRepositoryPort doctorRepository;

    @MockBean
    private LlmExtractionPort llmExtractionPort;

    @BeforeEach
    void setUp() {
        if (doctorRepository.findByNameIgnoreCase("Dr. Roberto").isEmpty()) {
            doctorRepository.save(new Doctor("doc-e2e-1", "Dr. Roberto", "Ortopedia"));
        }
    }

    @Test
    @DisplayName("Deve processar agendamento completo com sucesso através do endpoint REST")
    void shouldProcessFullScheduleFlowSuccessfully() throws Exception {
        ScheduleDetails scheduleDetails = new ScheduleDetails(
                "Dr. Roberto", "Ortopedia", LocalDate.of(2026, 12, 1), LocalTime.of(10, 0)
        );
        IntentExtractionResult extraction = new IntentExtractionResult(
                IntentType.SCHEDULE, "pt-BR", scheduleDetails, null, "Quero marcar consulta com Dr. Roberto"
        );
        when(llmExtractionPort.extractIntentAndParameters(anyString())).thenReturn(extraction);

        ChatScheduleRequest request = new ChatScheduleRequest(
                "conv-e2e-1", "pat-e2e-100", "Quero marcar consulta com Dr. Roberto dia 1 de dezembro às 10h"
        );

        mockMvc.perform(post("/api/v1/chat/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value("conv-e2e-1"))
                .andExpect(jsonPath("$.patientId").value("pat-e2e-100"))
                .andExpect(jsonPath("$.intent").value("SCHEDULE"))
                .andExpect(jsonPath("$.actionSuccess").value(true))
                .andExpect(jsonPath("$.humanizedResponse").value(org.hamcrest.Matchers.containsString("Consulta confirmada com sucesso")));
    }

    @Test
    @DisplayName("Deve rotear para fallback quando intenção for UNKNOWN")
    void shouldRouteToFallbackForUnknownIntent() throws Exception {
        IntentExtractionResult extraction = IntentExtractionResult.unknown("Fora de escopo");
        when(llmExtractionPort.extractIntentAndParameters(anyString())).thenReturn(extraction);

        ChatScheduleRequest request = new ChatScheduleRequest(
                "conv-e2e-2", "pat-e2e-200", "Como está o trânsito?"
        );

        mockMvc.perform(post("/api/v1/chat/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("UNKNOWN"))
                .andExpect(jsonPath("$.actionSuccess").value(false))
                .andExpect(jsonPath("$.humanizedResponse").value(org.hamcrest.Matchers.containsString("Não compreendi sua solicitação")));
    }

    @Test
    @DisplayName("Deve retornar 400 Bad Request se campos obrigatórios não forem fornecidos")
    void shouldReturnBadRequestWhenFieldsAreBlank() throws Exception {
        ChatScheduleRequest invalidRequest = new ChatScheduleRequest(
                "", "", ""
        );

        mockMvc.perform(post("/api/v1/chat/schedule")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
