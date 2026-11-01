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

## In Progress

- [ ] Definição e aprovação do Incremento 3.1: Integração Spring AI com OpenRouter e DTOs de Structured Output (`ScheduleCommand`, `IntentClassification`).

## Pending

- [ ] Milestone 3: Nós do Grafo para IA (`identifyIntentNode` e `messageGeneratorNode`).
- [ ] Milestone 4: Orquestração do DAG completo e Endpoint REST.

## Next Steps

1. **Red:** Escrever teste de integração de persistência para os adaptadores JPA e validação de schema.
2. **Green:** Criar script Flyway V1, entidades JPA, Spring Data Repositories e adaptadores de repositório.
3. **Refactor:** Revisar mapeamentos, indexação e validar build com `./mvnw clean test` e ArchUnit.
4. Atualizar documentação e diário de bordo.
