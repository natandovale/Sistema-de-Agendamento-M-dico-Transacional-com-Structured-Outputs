# Development Log

> Diário cronológico dos acontecimentos técnicos durante o desenvolvimento da aplicação.

---

## 2026-10-31

### [MEDIUM] [DOCS] Inicialização da Governança e Especificação Técnica

**Categoria:** Documentação e Arquitetura
**Importância:** Média
**Horário:** 17:25

**Descrição:**
Concluída a etapa de análise e alinhamento do projeto sob a metodologia XP.
Foram estabelecidas as decisões de adotar Spring Boot 3.4+, Java 21, Spring AI via OpenRouter e uma Engine de Grafo interna sob governança estrita de testes de arquitetura com ArchUnit.
Os documentos de governança foram estruturados:
- [AGENTS.md](file:///home/user/%C3%81rea%20de%20trabalho/temp/Projetos/Sistema-de-Agendamento-M-dico-Transacional-com-Structured-Outputs/docs/AGENTS.md)
- [ADR-001](file:///home/user/%C3%81rea%20de%20trabalho/temp/Projetos/Sistema-de-Agendamento-M-dico-Transacional-com-Structured-Outputs/docs/architecture/decisions/ADR-001-state-graph-orchestration-and-stack.md)
- [requirements.md](file:///home/user/%C3%81rea%20de%20trabalho/temp/Projetos/Sistema-de-Agendamento-M-dico-Transacional-com-Structured-Outputs/docs/requirements/requirements.md)
- [development-plan.md](file:///home/user/%C3%81rea%20de%20trabalho/temp/Projetos/Sistema-de-Agendamento-M-dico-Transacional-com-Structured-Outputs/docs/development/development-plan.md)
- [progress.md](file:///home/user/%C3%81rea%20de%20trabalho/temp/Projetos/Sistema-de-Agendamento-M-dico-Transacional-com-Structured-Outputs/docs/development/progress.md)

**Módulos afetados:**
- `docs/`

---

### [LOW] [CONFIG] Inclusão de Docker Compose para PostgreSQL Local

**Categoria:** Configuração e Infraestrutura
**Importância:** Baixa
**Horário:** 00:16

**Descrição:**
Adicionada a decisão de infraestrutura de incluir `docker-compose.yml` para provisionamento do PostgreSQL localmente, facilitando o desenvolvimento sem necessidade de instalação manual do banco. Atualizados AGENTS.md e ADR-001.

**Módulos afetados:**
- `docs/AGENTS.md`
- `docs/architecture/decisions/ADR-001-state-graph-orchestration-and-stack.md`

---

## 2026-11-01

### [MEDIUM] [BOOTSTRAP] Conclusão do Bootstrap da Aplicação Spring Boot

**Categoria:** Setup Inicial
**Importância:** Média
**Horário:** 00:21

**Descrição:**
Concluída com sucesso a etapa de Bootstrap do projeto.
Foram gerados:
- `docker-compose.yml` (PostgreSQL 16 com healthcheck)
- `.gitignore` (Java, Maven, IDEs, envs)
- `pom.xml` (Spring Boot 3.4.2, Java 21, Spring AI 1.0.0-M6, Postgres, Flyway, H2, ArchUnit, Testcontainers)
- `src/main/resources/application.yml` (perfis default e dev) e `src/test/resources/application-test.yml`
- Classe principal `SchedulerApplication.java`
- Teste de carga de contexto `SchedulerApplicationTests.java`
- Maven Wrapper (`mvnw`) gerado e validado com sucesso via `./mvnw clean test` (100% de sucesso).

**Módulos afetados:**
- Raiz do projeto, `src/main/`, `src/test/`

---

### [MEDIUM] [FEATURE] Incremento 1.1: Engine do StateGraph e Governança com ArchUnit

**Categoria:** Feature / Core
**Importância:** Média
**Horário:** 00:30

**Descrição:**
Implementado o primeiro incremento funcional sob metodologia TDD (Red-Green-Refactor):
- Criação dos testes `StateGraphTest` com 4 cenários (execução linear, bifurcação condicional para agendamento, cancelamento e tratamento de nós inexistentes).
- Criação das regras de arquitetura com `ArchitectureGovernanceTest` (ArchUnit).
- Implementação de `ScheduleState` como `record` imutável em Java 21 com derivação funcional de estado.
- Implementação de `Node<T>`, `EdgeCondition<T>` e `StateGraph<T>`.
- Todos os 7 testes foram executados e passaram com 100% de sucesso em 8.9s.

**Módulos afetados:**
- `com.med.scheduler.graph.core`
- `com.med.scheduler.graph.state`
- `com.med.scheduler.architecture`

---

### [MEDIUM] [FEATURE] Incremento 2.1: Modelagem e Regras Determinísticas do Domínio

**Categoria:** Feature / Domínio
**Importância:** Média
**Horário:** 00:46

**Descrição:**
Implementado o núcleo do domínio transacional com TDD:
- Criação dos testes `AppointmentDomainServiceTest` cobrindo: agendamento com sucesso, falha para médico inexistente, detecção determinística de conflito de horário/agenda ocupada, cancelamento com liberação de horário e validação contra cancelamento inexistente.
- Modelagem de entidades com Java 21: `Doctor`, `AppointmentStatus` e `Appointment` (com método cancel imutável).
- Criação das portas de saída `DoctorRepositoryPort` e `AppointmentRepositoryPort`.
- Criação de `AppointmentDomainService` isolado de qualquer dependência externa ou framework.
- 100% de sucesso nos 12 testes do projeto (incluindo ArchUnit e StateGraph).

**Módulos afetados:**
- `com.med.scheduler.domain.model`
- `com.med.scheduler.domain.repository`
- `com.med.scheduler.domain.service`

---

### [MEDIUM] [FEATURE] Incremento 2.2: Persistência Relacional com Spring Data JPA e Flyway

**Categoria:** Feature / Infraestrutura de Persistência
**Importância:** Média
**Horário:** 01:03

**Descrição:**
Implementada a camada de persistência relacional com desacoplamento arquitetural:
- Criação dos scripts de migração Flyway `V1__create_tables.sql` (tabelas `doctors` e `appointments` com chave estrangeira e índices).
- Criação das entidades JPA `DoctorEntity` e `AppointmentEntity` com mapeamento e isolamento bidirecional (`toDomain` e `fromDomain`).
- Criação dos repositórios `SpringDataDoctorRepository` e `SpringDataAppointmentRepository`.
- Criação dos adaptadores de persistência `DoctorPersistenceAdapter` e `AppointmentPersistenceAdapter` implementando as portas de saída do domínio.
- Criação da configuração `DomainServiceConfig` para injeção de dependência do serviço de domínio.
- Criação do teste de integração `AppointmentPersistenceIntegrationTest` validando transacionalidade e persistência.
- 100% de sucesso nos 14 testes do projeto em 13.6s.

**Módulos afetados:**
- `src/main/resources/db/migration/`
- `com.med.scheduler.infrastructure.persistence`
- `com.med.scheduler.infrastructure.config`

---

### [MEDIUM] [FEATURE] Incremento 3.1: Integração Spring AI com OpenRouter e Structured Outputs

**Categoria:** Feature / Integração IA
**Importância:** Média
**Horário:** 10:53

**Descrição:**
Implementada a integração de IA com contratos rígidos de Structured Outputs via Spring AI:
- Criação dos DTOs imutáveis com validações defensivas: `ScheduleDetails`, `CancelDetails` e `IntentExtractionResult`.
- Criação da porta de saída `LlmExtractionPort`.
- Criação de `OpenRouterStructuredExtractionService` utilizando a API fluente do `ChatClient` com mapeamento tipado (`.entity(IntentExtractionResult.class)`).
- Implementação de prompt de sistema com regras estritas para prevenção de alucinações e classificação precisa de intenções (`SCHEDULE`, `CANCEL`, `UNKNOWN`).
- Criação dos testes unitários em `LlmStructuredExtractionServiceTest` com 3 cenários (agendamento, cancelamento e fallback).
- 100% de sucesso nos 17 testes do projeto em 13.3s.

**Módulos afetados:**
- `com.med.scheduler.infrastructure.ai`
- `com.med.scheduler.infrastructure.ai.dto`
- `com.med.scheduler.infrastructure.ai.port`

---

### [MEDIUM] [FEATURE] Incremento 3.2: Implementação dos Nós Explícitos do Grafo (DAG)

**Categoria:** Feature / Grafo de Estados
**Importância:** Média
**Horário:** 11:03

**Descrição:**
Implementados os 5 nós operacionais do DAG conforme a especificação arquitetural sob governança do ArchUnit:
- `IdentifyIntentNode`: aciona a porta de IA e atualiza o estado com linguagem, intenção e payload estruturado.
- `SchedulerNode`: invoca o serviço de domínio determinístico para validação e persistência do agendamento, capturando exceções e registrando o resultado de negócio sem quebrar o grafo.
- `CancelNode`: executa o cancelamento determinístico de consultas.
- `FallbackNode`: orienta o paciente com instruções claras caso a intenção não seja identificada.
- `MessageGeneratorNode`: sintetiza a resposta humanizada adaptando o tom e o idioma identificado (pt-BR, en-US, es-ES).
- Criação dos testes unitários em `GraphNodesTest` cobrindo os 6 cenários dos nós.
- Validação pelo ArchUnit de que todas as 5 classes implementam `Node<ScheduleState>`.
- 100% de sucesso nos 23 testes do projeto em 12.4s.

**Módulos afetados:**
- `com.med.scheduler.graph.nodes`

---

### [MEDIUM] [FEATURE] Incremento 4.1: Orquestração do DAG e Endpoint REST com Testes E2E

**Categoria:** Feature / API & Orquestração
**Importância:** Média
**Horário:** 11:14

**Descrição:**
Concluída a entrega do Milestone 4 com a orquestração do DAG e a exposição da API REST:
- Criação de `ScheduleGraphOrchestrator` montando a máquina de estados completa com 5 nós, roteamento condicional para `schedulerNode`, `cancelNode` e `fallbackNode`, convergindo para `messageGeneratorNode`.
- Criação dos DTOs `ChatScheduleRequest` (com validações de campos obrigatórios via Bean Validation) e `ChatScheduleResponse`.
- Criação do controller REST `ChatScheduleController` no endpoint `POST /api/v1/chat/schedule`.
- Criação dos testes de integração de ponta a ponta em `ChatScheduleControllerE2ETest` com 3 cenários (agendamento com persistência real, fallback para intenção desconhecida e rejeição com 400 Bad Request para payload incompleto).
- Todos os 26 testes do projeto foram executados e passaram com 100% de sucesso em 14.7s.

**Módulos afetados:**
- `com.med.scheduler.graph`
- `com.med.scheduler.infrastructure.web`
- `com.med.scheduler.infrastructure.web.dto`

---

### [HIGH] [FIX] Resolução do Endpoint Base URL no OpenRouter e Validação Live

**Categoria:** Bugfix / Integração de IA
**Importância:** Alta
**Horário:** 12:02

**Descrição:**
Investigado o erro de chamada da LLM via OpenRouter.
1. **Causa Raiz Técnica:**
   O cliente `OpenAiApi` do Spring AI concatena nativamente `/v1/chat/completions` à propriedade `spring.ai.openai.base-url`.
   Quando configurado `https://openrouter.ai/api/v1`, o cliente montava a requisição para `https://openrouter.ai/api/v1/v1/chat/completions`, resultando em HTTP 404 Not Found do OpenRouter.
2. **Correção Efetuada:**
   - Atualizado `spring.ai.openai.base-url` para `https://openrouter.ai/api` (sem o `/v1` final) em `application.yml`.
   - Adicionada a chave de API fornecida como padrão seguro no `application.yml`.
   - Finalizado processo anterior remanescente na porta 8080 que impedia a aplicação de subir com as novas configurações.
   - Ajustado `AppointmentPersistenceIntegrationTest` para evitar duplicidade de nomes com os dados do seed Flyway.
3. **Validação:**
   - Suíte de 27 testes com 100% de aprovação (`BUILD SUCCESS`).
   - Aplicação executada em runtime com chamadas reais via `curl` demonstrando extração de `SCHEDULE` e `CANCEL`, consulta agendada com persistência e resposta humanizada.

**Módulos afetados:**
- `src/main/resources/application.yml`
- `src/test/java/com/med/scheduler/infrastructure/persistence/AppointmentPersistenceIntegrationTest.java`
- `src/test/java/com/med/scheduler/infrastructure/ai/OpenRouterLiveTest.java`
