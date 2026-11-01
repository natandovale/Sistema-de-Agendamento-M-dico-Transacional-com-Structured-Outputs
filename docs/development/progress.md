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

## In Progress

- [ ] Definição e aprovação do Primeiro Incremento (Incremento 1.1: Engine de Grafo de Estados com TDD).

## Pending

- [ ] Milestone 1: Regras e testes de arquitetura com ArchUnit.
- [ ] Milestone 1: Engine de Grafo de Estados (`ScheduleState`, `Node<T>`, `Edge<T>`, `StateGraphEngine`).
- [ ] Milestone 2: Domínio Transacional de Agendamentos.
- [ ] Milestone 3: Integração Spring AI com OpenRouter e Structured Outputs.
- [ ] Milestone 4: Orquestração do DAG completo e Endpoint REST.

## Next Steps

1. Obter aprovação do desenvolvedor para a documentação gerada.
2. Executar o **Bootstrap** (Etapa 5): criar o `pom.xml`, estrutura de pastas padrão e classe principal Spring Boot sem regras de negócio ainda.
3. Alinhar a proposta do **Primeiro Incremento Funcional** (Etapa 6).
