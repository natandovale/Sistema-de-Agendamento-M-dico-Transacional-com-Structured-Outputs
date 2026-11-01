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
- [x] **Incremento 1.1 (Primeiro Incremento):** Engine de Grafo de Estados (`ScheduleState`, `Node<T>`, `EdgeCondition<T>`, `StateGraph<T>`) implementada com TDD estrito e governança via ArchUnit (100% dos testes verdes).

## In Progress

- [ ] Definição e aprovação do Incremento 2.1: Modelagem do Domínio Transacional (`Doctor`, `TimeSlot`, `Appointment`) com TDD.

## Pending

- [ ] Milestone 2: Domínio Transacional de Agendamentos (`AppointmentDomainService`, repositórios e anti-double booking).
- [ ] Milestone 3: Integração Spring AI com OpenRouter e Structured Outputs.
- [ ] Milestone 4: Orquestração do DAG completo e Endpoint REST.

## Next Steps

1. **Red:** Escrever testes de unidade para `StateGraph` e testes de arquitetura com `ArchUnit`.
2. **Green:** Implementar `ScheduleState`, `Node<T>`, `EdgeCondition<T>` e `StateGraph<T>`.
3. **Refactor:** Revisar design, garantir imutabilidade estrita e validar build com `./mvnw clean test`.
4. Atualizar documentação e diário de bordo.
