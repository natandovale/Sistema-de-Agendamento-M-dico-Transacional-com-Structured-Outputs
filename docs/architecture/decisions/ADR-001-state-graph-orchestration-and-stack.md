# ADR-001: Arquitetura, Stack Tecnológica e Orquestração do Grafo de Estados

- **Status:** Aprovado
- **Data:** 2026-10-31
- **Decisores:** Natan do Vale e AI Pair Programmer

---

## 1. Contexto

O projeto consiste em um microsserviço corporativo que processa texto livre de pacientes e converte em ações transacionais determinísticas de agendamento e cancelamento de consultas médicas.
A premissa fundamental do produto é que o LLM não pode executar regras de negócio nem persistência diretamente; seu papel é restrito a atuar como tradutor de linguagem natural para contratos JSON rígidos (Structured Outputs). O fluxo deve ser orquestrado por um Grafo de Estados (DAG).

## 2. Decisão

1. **Framework e Linguagem:**
   - Adotar **Java 21 LTS** e **Spring Boot 3.4+**.
   - Justificativa: Aproveitamento de `record`s imutáveis, Pattern Matching, Virtual Threads e integração de primeira classe com o Spring AI.

2. **Integração de IA:**
   - Adotar **Spring AI 1.0.0-M6+** conectado à API do **OpenRouter** (usando modelo OpenAI-compatible com endpoint base customizado `https://openrouter.ai/api/v1`).
   - Utilizar Structured Outputs tipados com Java Records para validação estrita de schema.

3. **Orquestração de Máquina de Estados (Grafo / DAG):**
   - Implementar uma **Engine de Grafo Corporativa Interna** (State Pattern com interfaces `Node<ScheduleState>`, `EdgeCondition<ScheduleState>` e `StateGraphEngine`).
   - Evitar dependências experimentais externas (como `langgraph4j` ou `spring-statemachine`) para garantir simplicidade, manutenibilidade e performance em milissegundos sem reflexão pesada.
   - Adotar **ArchUnit** para forçar governança e garantir que novos nós e dependências obedeçam estritamente à arquitetura, impedindo desvios por diferentes desenvolvedores do time.

4. **Domínio de Agendamento e Persistência:**
   - O microsserviço conterá seu próprio domínio de agendamento (`Doctor`, `TimeSlot`, `Appointment`).
   - Persistência com **Spring Data JPA** e versionamento de schema com **Flyway**.
   - Suporte a **H2** para testes rápidos e **PostgreSQL 16** provisionado localmente via **Docker Compose** e em produção.
   - Testes de integração utilizando **Testcontainers (PostgreSQL)** para simulação fidedigna de banco real.
   - Prevenção de conflito (*double booking*) via restrição de unicidade no banco de dados e controle transacional.

5. **Interface de Comunicação:**
   - API REST síncrona com endpoint `POST /api/v1/chat/schedule`.

## 3. Alternativas Consideradas

- **LangChain4j:** Avaliado para orquestração e chamadas de LLM. Descartado para evitar sobreposição e conflito de clientes com o Spring AI no ecossistema Spring Boot 3.4+. Além disso, LangChain4j não possui um motor nativo de grafo de estados equivalente ao LangGraph.
- **LangGraph4j (port comunitário):** Descartado por se tratar de biblioteca mantida individualmente, baseada em assincronismo reativo e dicionários genéricos `Map<String, Object>`, contrariando a forte tipagem com Java 21 `record`s e virtual threads.
- **Spring Statemachine:** Descartado por ser uma biblioteca complexa baseada em especificações UML antigas, gerando overengineering para o fluxo acíclico de 5 nós.

## 4. Consequências

- **Positivas:**
  - Código limpo, testável em microssegundos com TDD.
  - Zero risco de alucinações executando código de banco.
  - Governança garantida automaticamente por testes de arquitetura (ArchUnit).
- **Negativas / Desafios:**
  - Necessidade de codificar as interfaces base da engine de grafo no primeiro incremento (um esforço baixo de ~3 classes).
