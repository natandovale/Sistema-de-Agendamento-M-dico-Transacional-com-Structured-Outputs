# Project Progress

## Current Status

- **Status:** COMPLETED
- **Last Update:** 2026-11-01
- **Current Milestone:** Projeto Finalizado com Sucesso (100% de cobertura nos requisitos)

## Completed

- [x] Análise detalhada do projeto e levantamento de regras de negócio.
- [x] Alinhamento arquitetural (Spring Boot 3.4+, Java 21, Spring AI + OpenRouter, Engine de Grafo interna, Domínio próprio, REST API).
- [x] Documentação do `docs/AGENTS.md` e ADR-001.
- [x] Elaboração do documento de requisitos e plano de desenvolvimento.
- [x] Inclusão de `docker-compose.yml` para provisionamento do PostgreSQL local.
- [x] **Bootstrap concluído:** Setup de `pom.xml`, Maven Wrapper, perfis `application.yml` / `application-test.yml`, estrutura base e compilação/teste validado com sucesso.
- [x] **Incremento 1.1 (Engine de Grafo):** `ScheduleState`, `Node<T>`, `EdgeCondition<T>`, `StateGraph<T>` e ArchUnit (100% testado).
- [x] **Incremento 2.1 (Domínio Puro):** `Doctor`, `Appointment`, `AppointmentStatus`, portas de repositório e `AppointmentDomainService` (regras determinísticas de disponibilidade, conflito e cancelamento 100% testadas com TDD).
- [x] **Incremento 2.2 (Persistência Relacional):** `DoctorEntity`, `AppointmentEntity`, Spring Data Repositories, adaptadores de persistência, Flyway migration V1 e testes de integração de persistência (100% testados).
- [x] **Incremento 3.1 (Spring AI & Structured Outputs):** `ScheduleDetails`, `CancelDetails`, `IntentExtractionResult`, `LlmExtractionPort` e `OpenRouterStructuredExtractionService` com extração tipada (100% testados).
- [x] **Incremento 3.2 (Nós do Grafo):** `IdentifyIntentNode`, `SchedulerNode`, `CancelNode`, `FallbackNode` e `MessageGeneratorNode` integrados aos serviços e validados via ArchUnit (100% testados).
- [x] **Incremento 4.1 (Orquestração do DAG & API REST):** `ScheduleGraphOrchestrator`, `ChatScheduleController`, DTOs de API e testes E2E com `MockMvc` (100% testados).
- [x] Elaboração do `README.md` com instruções completas de execução e consumo via cURL.

## In Progress

- [ ] Nenhuma atividade em andamento.

## Pending

- [ ] Todas as entregas previstas foram implementadas e testadas.

## Next Steps

1. Criar o `README.md` com guia de execução, comandos do Docker e exemplos de chamadas à API REST.
2. Apresentar o resumo da entrega completa para alinhamento final.
