# Project Progress

## Current Status

- **Status:** DEVELOPMENT
- **Last Update:** 2026-11-01
- **Current Milestone:** Milestone 1 — Bootstrap & Arquitetura Base

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

## In Progress

- [ ] Definição e aprovação do Incremento 4.1 (Milestone 4): Montagem do Grafo Completo (`ScheduleGraphOrchestrator`), Endpoint REST (`ChatScheduleController`) e testes E2E.

## Pending

- [ ] Finalização do projeto e documentação final de encerramento.

## Next Steps

1. **Red:** Escrever testes de unidade para cada um dos 5 nós do Grafo com mocks de IA e Domínio.
2. **Green:** Implementar as classes de nós em `com.med.scheduler.graph.nodes` implementando `Node<ScheduleState>`.
3. **Refactor:** Revisar tratamento de erros defensivo e validar build com `./mvnw clean test` e regras do ArchUnit.
4. Atualizar documentação e diário de bordo.
