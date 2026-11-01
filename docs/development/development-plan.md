# Plano de Desenvolvimento — Extreme Programming

> Planejamento iterativo de entregas em pequenos incrementos (Small Releases) guiadas por TDD.

---

## Milestone 1: Bootstrap & Arquitetura Base
- **Incremento 1.1:** Setup do projeto Spring Boot 3.4+ com Java 21, dependências essenciais (`pom.xml`), configuração de perfis (dev/test) e validação de build limpo.
- **Incremento 1.2:** Estrutura e testes de arquitetura com ArchUnit (garantindo regras de isolamento do domínio e convenções do grafo).
- **Incremento 1.3:** Engine de Grafo de Estados (`ScheduleState`, `Node<T>`, `Edge<T>`, `StateGraphEngine`) e testes unitários com TDD.

## Milestone 2: Domínio Transacional de Agendamentos (Lógica Determinística)
- **Incremento 2.1:** Entidades de domínio puras (`Doctor`, `TimeSlot`, `Appointment`) e Value Objects.
- **Incremento 2.2:** `AppointmentDomainService` com regras determinísticas de disponibilidade, agendamento e cancelamento (TDD puro sem dependências externas).
- **Incremento 2.3:** Persistência relacional com Spring Data JPA, migrations Flyway e teste de concorrência simultânea (anti-double booking) com Testcontainers.

## Milestone 3: Integração com LLM & Structured Outputs (OpenRouter)
- **Incremento 3.1:** Configuração do `ChatClient` do Spring AI com OpenRouter e DTOs de Structured Output (`ScheduleCommand`, `IntentClassification`).
- **Incremento 3.2:** Implementação do nó `identifyIntentNode` com prompts e extração validada de schema.
- **Incremento 3.3:** Implementação do nó `messageGeneratorNode` adaptado ao idioma do paciente.

## Milestone 4: Orquestração Completa do DAG & API REST
- **Incremento 4.1:** Nós executores de domínio no grafo: `schedulerNode`, `cancelNode` e `fallbackNode`.
- **Incremento 4.2:** Montagem do Grafo de Estados completo e testes end-to-end de orquestração.
- **Incremento 4.3:** Endpoint REST `POST /api/v1/chat/schedule` e validação com MockMvc.
