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

## In Progress

- [ ] Definição e aprovação do Incremento 3.2: Implementação dos nós do Grafo com IA e Domínio (`IdentifyIntentNode`, `SchedulerNode`, `CancelNode`, `FallbackNode`, `MessageGeneratorNode`).

## Pending

- [ ] Milestone 4: Orquestração do DAG completo e Endpoint REST.

## Next Steps

1. **Red:** Escrever testes de unidade para `OpenRouterStructuredExtractionService` com mocks de `ChatClient`.
2. **Green:** Criar os records de Structured Output, porta `LlmExtractionPort` e serviço `OpenRouterStructuredExtractionService`.
3. **Refactor:** Revisar prompts, validações de schema e validar build com `./mvnw clean test` e ArchUnit.
4. Atualizar documentação e diário de bordo.
