# Especificação de Requisitos do Sistema

## 1. Visão Geral
Sistema de agendamento médico com processamento transacional de linguagem natural via Structured Outputs e orquestração de máquina de estados (DAG).

---

## 2. Requisitos Funcionais (RF)

- **RF01 — Recebimento de Mensagens:**
  - O sistema deve expor um endpoint REST `POST /api/v1/chat/schedule` que recebe:
    - `conversationId` (String, obrigatório)
    - `patientId` (String, obrigatório)
    - `message` (String, obrigatório, texto livre do paciente)
  - Deve retornar a resposta humanizada, status da transação e metadados do processamento.

- **RF02 — Identificação de Intenção e Extração Estruturada:**
  - O nó `identifyIntentNode` deve processar a mensagem do paciente usando LLM com Structured Outputs.
  - Deve classificar a intenção em: `SCHEDULE`, `CANCEL` ou `UNKNOWN`.
  - Para `SCHEDULE`, deve extrair campos como: `doctorName`, `specialty`, `desiredDate` (ISO-8601 ou relativo), `desiredTime`.
  - Para `CANCEL`, deve extrair: `doctorName`, `appointmentDate` ou `appointmentId` se fornecido.
  - Para `UNKNOWN` ou dados incoerentes, deve preencher intenção desconhecida.

- **RF03 — Validação Determinística de Disponibilidade e Agendamento:**
  - O nó `schedulerNode` deve consultar o serviço de domínio `AppointmentDomainService`.
  - Deve verificar se o médico existe e possui slot disponível no dia e hora solicitados.
  - Se disponível, deve reservar a consulta no banco de dados.
  - Se não houver horário disponível ou médico não for encontrado, deve registrar motivo de recusa determinístico no estado.

- **RF04 — Cancelamento Determinístico:**
  - O nó `cancelNode` deve localizar a consulta ativa do paciente e marcá-la como cancelada no domínio.
  - O horário deve ser liberado para novas consultas.

- **RF05 — Tratamento de Fallback:**
  - O nó `fallbackNode` deve ser acionado caso a intenção não seja identificada ou dados mínimos essenciais não estejam presentes, gerando orientação clara ao usuário.

- **RF06 — Geração de Mensagem Humanizada:**
  - O nó `messageGeneratorNode` deve receber o resultado da operação de domínio e gerar uma resposta em linguagem natural amigável.
  - Deve manter o idioma original identificado na mensagem do paciente (ex: Português, Inglês, Espanhol).

- **RF07 — Rastreamento de Estado Compartilhado:**
  - O estado do grafo (`ScheduleState`) deve registrar:
    - Mensagens da conversa
    - Comando extraído
    - Resultado da ação de domínio
    - Flag de sucesso (`actionSuccess`)
    - Resposta humanizada final

---

## 3. Requisitos Não Funcionais (RNF)

- **RNF01 (Stack Base):** Java 21 LTS e Spring Boot 3.4+.
- **RNF02 (Integração IA):** Spring AI configurado para OpenRouter API.
- **RNF03 (Isolamento de Domínio):** Nenhuma classe de domínio pode importar frameworks de infraestrutura ou bibliotecas de LLM.
- **RNF04 (Prevenção de Concorrência):** Prevenção de agendamentos simultâneos no mesmo horário para o mesmo médico através de constraint única relacional e transações com isolamento adequado.
- **RNF05 (Persistência):** Suporte a PostgreSQL para produção e H2 para ambiente de desenvolvimento e testes. Migrações automáticas via Flyway.
- **RNF06 (Conformidade com TDD):** Todo incremento de código deve ser precedido por testes de unidade/integração.
- **RNF07 (Governança Automatizada):** Testes com ArchUnit devem validar continuamente a integridade da arquitetura hexagonal.
