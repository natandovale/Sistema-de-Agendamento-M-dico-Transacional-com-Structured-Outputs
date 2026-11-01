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

## In Progress

- [ ] Definição e aprovação do Incremento 2.2: Persistência relacional com Spring Data JPA, entidades JPA, migrações Flyway e prevenção de concorrência com Testcontainers.

## Pending

- [ ] Milestone 3: Integração Spring AI com OpenRouter e Structured Outputs.
- [ ] Milestone 4: Orquestração do DAG completo e Endpoint REST.

## Next Steps

1. **Red:** Escrever testes de unidade para `AppointmentDomainService` (agendamento com sucesso, conflito de horário, cancelamento e médico inexistente).
2. **Green:** Implementar entidades de domínio (`Doctor`, `TimeSlot`, `Appointment`), interfaces de repositório e `AppointmentDomainService`.
3. **Refactor:** Revisar design defensivo do domínio e validar build com `./mvnw clean test` e ArchUnit.
4. Atualizar documentação e diário de bordo.
