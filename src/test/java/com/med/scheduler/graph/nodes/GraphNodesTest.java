package com.med.scheduler.graph.nodes;

import com.med.scheduler.domain.model.Appointment;
import com.med.scheduler.domain.model.AppointmentStatus;
import com.med.scheduler.domain.model.Doctor;
import com.med.scheduler.domain.service.AppointmentDomainService;
import com.med.scheduler.graph.state.IntentType;
import com.med.scheduler.graph.state.ScheduleState;
import com.med.scheduler.infrastructure.ai.dto.CancelDetails;
import com.med.scheduler.infrastructure.ai.dto.IntentExtractionResult;
import com.med.scheduler.infrastructure.ai.dto.ScheduleDetails;
import com.med.scheduler.infrastructure.ai.port.LlmExtractionPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphNodesTest {

    @Mock
    private LlmExtractionPort llmExtractionPort;

    @Mock
    private AppointmentDomainService appointmentDomainService;

    @Test
    @DisplayName("IdentifyIntentNode deve atualizar o estado com idioma, intenção e comando extraído")
    void identifyIntentNodeShouldUpdateState() {
        IdentifyIntentNode node = new IdentifyIntentNode(llmExtractionPort);

        ScheduleDetails scheduleDetails = new ScheduleDetails(
                "Dr. Alício", "Cardiologia", LocalDate.of(2026, 11, 10), LocalTime.of(14, 0)
        );
        IntentExtractionResult extraction = new IntentExtractionResult(
                IntentType.SCHEDULE, "pt-BR", scheduleDetails, null, "Quero marcar consulta"
        );
        when(llmExtractionPort.extractIntentAndParameters("Quero marcar consulta")).thenReturn(extraction);

        ScheduleState state = ScheduleState.initial("c1", "p1", "Quero marcar consulta");
        ScheduleState updated = node.execute(state);

        assertThat(updated.intent()).isEqualTo(IntentType.SCHEDULE);
        assertThat(updated.detectedLanguage()).isEqualTo("pt-BR");
        assertThat(updated.command()).isEqualTo(extraction);
    }

    @Test
    @DisplayName("SchedulerNode deve agendar consulta com sucesso quando comando válido")
    void schedulerNodeShouldBookAppointmentSuccessfully() {
        SchedulerNode node = new SchedulerNode(appointmentDomainService);

        LocalDate date = LocalDate.of(2026, 11, 10);
        LocalTime time = LocalTime.of(14, 0);
        ScheduleDetails scheduleDetails = new ScheduleDetails("Dr. Alício", "Cardiologia", date, time);
        IntentExtractionResult extraction = new IntentExtractionResult(
                IntentType.SCHEDULE, "pt-BR", scheduleDetails, null, "Consulta com Dr. Alício"
        );

        Doctor doctor = new Doctor("doc-1", "Dr. Alício", "Cardiologia");
        Appointment appointment = new Appointment("app-1", doctor, "p1", LocalDateTime.of(date, time), AppointmentStatus.CONFIRMED);
        when(appointmentDomainService.schedule(eq("Dr. Alício"), eq("p1"), eq(LocalDateTime.of(date, time))))
                .thenReturn(appointment);

        ScheduleState state = ScheduleState.initial("c1", "p1", "Quero marcar")
                .withIntent(IntentType.SCHEDULE)
                .withCommand(extraction);

        ScheduleState updated = node.execute(state);

        assertThat(updated.actionSuccess()).isTrue();
        assertThat(updated.actionResult()).contains("Consulta confirmada com sucesso");
    }

    @Test
    @DisplayName("SchedulerNode deve capturar recusa de conflito de horário sem quebrar o grafo")
    void schedulerNodeShouldHandleConflictGracefully() {
        SchedulerNode node = new SchedulerNode(appointmentDomainService);

        LocalDate date = LocalDate.of(2026, 11, 10);
        LocalTime time = LocalTime.of(14, 0);
        ScheduleDetails scheduleDetails = new ScheduleDetails("Dr. Alício", "Cardiologia", date, time);
        IntentExtractionResult extraction = new IntentExtractionResult(
                IntentType.SCHEDULE, "pt-BR", scheduleDetails, null, "Consulta"
        );

        when(appointmentDomainService.schedule(eq("Dr. Alício"), eq("p1"), eq(LocalDateTime.of(date, time))))
                .thenThrow(new IllegalStateException("Horário indisponível para o médico Dr. Alício."));

        ScheduleState state = ScheduleState.initial("c1", "p1", "Quero marcar")
                .withIntent(IntentType.SCHEDULE)
                .withCommand(extraction);

        ScheduleState updated = node.execute(state);

        assertThat(updated.actionSuccess()).isFalse();
        assertThat(updated.actionResult()).contains("Horário indisponível");
    }

    @Test
    @DisplayName("CancelNode deve cancelar consulta quando dados forem válidos")
    void cancelNodeShouldCancelSuccessfully() {
        CancelNode node = new CancelNode(appointmentDomainService);

        CancelDetails cancelDetails = new CancelDetails("app-1", "Dr. Alício", null);
        IntentExtractionResult extraction = new IntentExtractionResult(
                IntentType.CANCEL, "pt-BR", null, cancelDetails, "Cancelar"
        );

        Doctor doctor = new Doctor("doc-1", "Dr. Alício", "Cardiologia");
        Appointment cancelledAppointment = new Appointment("app-1", doctor, "p1", LocalDateTime.now(), AppointmentStatus.CANCELLED);
        when(appointmentDomainService.cancel("app-1", "p1")).thenReturn(cancelledAppointment);

        ScheduleState state = ScheduleState.initial("c1", "p1", "Cancelar")
                .withIntent(IntentType.CANCEL)
                .withCommand(extraction);

        ScheduleState updated = node.execute(state);

        assertThat(updated.actionSuccess()).isTrue();
        assertThat(updated.actionResult()).contains("Consulta cancelada com sucesso");
    }

    @Test
    @DisplayName("FallbackNode deve orientar usuário para mensagens não compreendidas")
    void fallbackNodeShouldProvideGuidance() {
        FallbackNode node = new FallbackNode();

        ScheduleState state = ScheduleState.initial("c1", "p1", "Previsão do tempo");
        ScheduleState updated = node.execute(state);

        assertThat(updated.actionSuccess()).isFalse();
        assertThat(updated.actionResult()).contains("Não compreendi sua solicitação");
    }

    @Test
    @DisplayName("MessageGeneratorNode deve gerar mensagem humanizada adaptada ao idioma")
    void messageGeneratorNodeShouldFormatHumanizedResponse() {
        MessageGeneratorNode node = new MessageGeneratorNode();

        ScheduleState state = ScheduleState.initial("c1", "p1", "Marcar consulta")
                .withDetectedLanguage("pt-BR")
                .withActionResult("Consulta confirmada com sucesso: Dr. Alício às 14:00.", true);

        ScheduleState updated = node.execute(state);

        assertThat(updated.humanizedResponse()).isNotNull();
        assertThat(updated.humanizedResponse()).contains("Consulta confirmada");
    }
}
