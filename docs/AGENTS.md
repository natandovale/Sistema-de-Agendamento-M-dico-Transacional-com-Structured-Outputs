# AGENTS.md — Project Governance & AI Pair Programming

> Documento central de governança técnica, arquitetura e colaboração com agentes de IA.
>
> Este documento estabelece as diretrizes que devem ser seguidas por qualquer agente de IA que participe do desenvolvimento deste projeto.
>
> **Regra principal:** Nenhuma implementação deve começar sem que o agente compreenda o contexto, valide os requisitos e alinhe a abordagem técnica com o desenvolvedor.

---

# 1. Project Overview

## 1.1. Identificação

- **Nome do projeto:** Sistema de Agendamento Médico Transacional com Structured Outputs
- **Descrição:** Microsserviço corporativo que traduz texto livre de pacientes em ações transacionais determinísticas de agendamento e cancelamento de consultas médicas.
- **Objetivo principal:** Garantir separação rígida entre IA (tradução de linguagem natural para JSON tipado via Structured Outputs) e Domínio Transacional (regras de disponibilidade, conflitos de concorrência e persistência), orquestrado por um Grafo de Estados (DAG).
- **Público-alvo:** Pacientes e canais de atendimento (chatbots, WhatsApp, portais web) que consomem a API de agendamento.
- **Status:** PRODUCTION
- **Responsável técnico:** Natan do Vale

## 1.2. Visão do Produto

Sistemas conversacionais convencionais frequentemente delegam decisões de negócio diretamente ao modelo de linguagem (LLM), provocando alucinações, falhas de consistência e conflitos em dados relacionais de agendamento.

Este produto resolve o problema desacoplando a IA do domínio:
1. O LLM atua **exclusivamente como extrator e tradutor de linguagem natural** para contratos rígidos JSON (Structured Outputs).
2. Uma **Máquina de Estados / DAG** executa o fluxo determinístico composto pelos nós: `identifyIntent`, `schedulerNode`, `cancelNode`, `fallbackNode` e `messageGeneratorNode`.
3. As regras de verificação real de disponibilidade, tratamento de concorrência (prevenção de *double booking*) e persistência são executadas no núcleo determinístico do domínio em Java.
4. O resultado determinístico é humanizado em resposta ao paciente, respeitando seu idioma de comunicação.

## 1.3. Escopo

### Funcionalidades principais
- Identificação determinística de intenção (`SCHEDULE`, `CANCEL`, `UNKNOWN`).
- Extração de comandos estruturados validados por schema JSON (médico, especialidade, data, horário).
- Validação real de disponibilidade de médicos e horários.
- Agendamento atômico com prevenção de agendamento concorrente simultâneo (*double booking*).
- Cancelamento determinístico de consultas existentes.
- Roteamento seguro de fallback para solicitações ambíguas ou inválidas.
- Geração de mensagem humanizada no idioma do paciente informando o status da ação.

### Fora do escopo
- Autenticação e autorização complexa multi-tenant na fase inicial (será focado em API síncrona com identificação do paciente via payload).
- Integração de pagamentos ou prontuário eletrônico completo (PEP).
- Execução de ações de escrita no banco diretamente pela LLM (terminantemente proibido).

### Restrições e premissas
- **Java 21 LTS** e **Spring Boot 3.4+**.
- **LLM Provider:** OpenRouter API (utilizando modelos com suporte nativo a JSON Schema / Structured Outputs).
- **Persistência:** PostgreSQL para produção/staging e H2 para testes e desenvolvimento local rápido.
- A orquestração do DAG é implementada em código Java idiomático com contratos rígidos de interfaces imutáveis e governança com ArchUnit.

---

# 2. Architecture & Technical Decisions

## 2.1. Padrão Arquitetural

- **Arquitetura adotada:** Clean Architecture / Hexagonal (Ports & Adapters) modular.
- **Estilo de comunicação:** REST síncrono (`POST /api/v1/chat/schedule`).
- **Organização:** Monorepo modular por pacotes.
- **Estratégia de persistência:** Relacional (PostgreSQL / H2) gerenciado via Spring Data JPA e versionado via Flyway.

### Princípios arquiteturais

1. **Separação de responsabilidades (IA vs. Domínio):** O domínio não conhece Spring AI nem OpenRouter. A IA não conhece o banco de dados.
2. **Imutabilidade de Estado:** O estado do grafo (`ScheduleState`) é um Java `record` imutável passado entre os nós.
3. **Baixo acoplamento e Alta coesão:** Cada nó do grafo é um componente independente com responsabilidade única.
4. **Inversão de dependência:** Módulos de infraestrutura e IA implementam interfaces definidas pelo domínio ou pela engine do grafo.
5. **Simplicidade e Testabilidade:** Módulos de domínio e de grafo testáveis sem necessidade de subir o contexto Spring completo ou fazer chamadas reais de rede.

## 2.2. Regras de Dependência

```text
infrastructure (Web, Spring AI, JPA)
        │                │
        ▼                ▼
      graph  ────────► domain (Puro Java 21)
```

Regras obrigatórias:
- O pacote `domain` **não pode** importar classes de `infrastructure`, `graph` ou de frameworks de terceiros (Spring, Jackson, Spring AI).
- O pacote `graph` pode depender de `domain` para chamar serviços de negócio, mas não de `infrastructure.web`.
- A infraestrutura de IA (`infrastructure.ai`) implementa as portas de comunicação com LLM requeridas pelos nós do grafo.
- Essas regras devem ser garantidas por testes automatizados com **ArchUnit**.

## 2.3. Architecture Decision Records (ADR)

Decisões arquiteturais documentadas em: `docs/architecture/decisions/`

- **ADR-001:** Adoção de Spring Boot 3.4+, Java 21, Spring AI via OpenRouter e Engine de Grafo Nativa com ArchUnit.

---

# 3. Technology Stack

## 3.1. Tecnologias

| Camada | Tecnologia | Versão | Justificativa |
|---|---|---|---|
| Linguagem | Java | 21 LTS | Records, Pattern Matching, Sequenced Collections e Virtual Threads |
| Framework | Spring Boot | 3.4.x | Ecossistema padrão corporativo, integração nativa Spring AI |
| IA & LLM | Spring AI | 1.0.0-M6+ | ChatClient com suporte a Structured Outputs e OpenRouter API |
| Manipulação JSON | Jackson Databind | 2.18+ | Manipulação de contratos JSON e serialização de records |
| Database | PostgreSQL / H2 | PG 16 / H2 2.3+ | PostgreSQL para produção; H2 in-memory para testes unitários/locais |
| Infrastructure | Docker & Docker Compose | 24+ | Provisionamento local do PostgreSQL com zero atrito de setup |
| Migrations | Flyway | 10.x | Versionamento determinístico de schema DDL |
| Testes Unitários | JUnit 5 + Mockito + AssertJ | Modern | Testes rápidos seguindo TDD |
| Testes Integração | Testcontainers (PostgreSQL) | 1.20+ | Validação de concorrência e transações reais |
| Governança | ArchUnit | 1.3+ | Prevenção de quebra de regras arquiteturais e padronização do grafo |

## 3.2. Bibliotecas e Frameworks

| Biblioteca | Responsabilidade | Módulo |
|---|---|---|
| `spring-boot-starter-web` | Exposição dos endpoints REST | Infrastructure Web |
| `spring-boot-starter-validation` | Validação de inputs da API e DTOs | Infrastructure Web |
| `spring-boot-starter-data-jpa` | Persistência de agendamentos e histórico | Infrastructure Persistence |
| `spring-ai-openai-spring-boot-starter` | Comunicação com OpenRouter via API OpenAI compatível | Infrastructure AI |
| `flyway-core` + `flyway-database-postgresql` | Migrações de banco de dados | Infrastructure Persistence |
| `archunit-junit5` | Validação automatizada de regras arquiteturais | Tests Architecture |

---

# 4. Repository Structure

## 4.1. Estrutura de Diretórios

```text
Sistema-de-Agendamento-M-dico-Transacional-com-Structured-Outputs/
├── docs/
│   ├── AGENTS.md
│   ├── architecture/
│   │   └── decisions/
│   │       └── ADR-001-state-graph-orchestration-and-stack.md
│   ├── requirements/
│   │   └── requirements.md
│   └── development/
│       ├── development-plan.md
│       ├── progress.md
│       └── development-log.md
├── src/
│   ├── main/
│   │   ├── java/com/med/scheduler/
│   │   │   ├── domain/
│   │   │   │   ├── model/                 # Entidades e Value Objects puros (Appointment, Doctor, Slot)
│   │   │   │   ├── repository/            # Interfaces de repositório do domínio
│   │   │   │   └── service/               # AppointmentDomainService (regras determinísticas)
│   │   │   ├── graph/
│   │   │   │   ├── core/                  # Node, Edge, StateGraph, GraphEngine
│   │   │   │   ├── state/                 # ScheduleState (Java record imutável)
│   │   │   │   └── nodes/                 # IdentifyIntentNode, SchedulerNode, CancelNode, FallbackNode, MessageGeneratorNode
│   │   │   ├── infrastructure/
│   │   │   │   ├── ai/                    # OpenRouter Client, Prompts e DTOs de Structured Output
│   │   │   │   ├── persistence/           # Entidades JPA, Repositories Spring Data, Adapters
│   │   │   │   └── web/                   # Controller REST, DTOs de Request/Response
│   │   │   └── SchedulerApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   └── test/
│       ├── java/com/med/scheduler/
│       │   ├── domain/                    # Testes puros de domínio (sem Spring)
│       │   ├── graph/                     # Testes do grafo e dos nós
│       │   ├── architecture/              # Testes com ArchUnit
│       │   └── integration/               # Testes de integração (REST + Testcontainers)
│       └── resources/
│           └── application-test.yml
├── pom.xml
└── README.md
```

## 4.2. Organização de Arquivos

- **Models:** `com.med.scheduler.domain.model`
- **Entities (JPA):** `com.med.scheduler.infrastructure.persistence.entity`
- **DTOs:** `com.med.scheduler.infrastructure.web.dto` e `com.med.scheduler.infrastructure.ai.dto`
- **Interfaces de Domínio:** `com.med.scheduler.domain.repository` e `com.med.scheduler.domain.port`
- **Services de Domínio:** `com.med.scheduler.domain.service`
- **Nós do Grafo:** `com.med.scheduler.graph.nodes`
- **Controllers:** `com.med.scheduler.infrastructure.web`
- **Configurations:** `com.med.scheduler.infrastructure.config`
- **Tests:** `src/test/java/com/med/scheduler/...`

---

# 5. Environment & Configuration

## 5.1. Variáveis de Ambiente

| Variável | Descrição | Obrigatória | Padrão Local |
|---|---|---|---|
| `OPENROUTER_API_KEY` | Chave de autenticação na OpenRouter API | Sim (para produção/IA) | - |
| `OPENROUTER_BASE_URL` | URL base da OpenRouter | Não | `https://openrouter.ai/api/v1` |
| `OPENROUTER_MODEL` | Modelo padrão a ser utilizado | Não | `meta-llama/llama-3.1-8b-instruct` |
| `SPRING_DATASOURCE_URL` | URL de conexão com o banco | Sim (produção) | `jdbc:h2:mem:schedulerdb;DB_CLOSE_DELAY=-1` |
| `SPRING_DATASOURCE_USERNAME` | Usuário do banco | Sim (produção) | `sa` |
| `SPRING_DATASOURCE_PASSWORD` | Senha do banco | Sim (produção) | `""` |
| `SERVER_PORT` | Porta HTTP da aplicação | Não | `8080` |

---

# 6. Domain Rules & Business Logic

## 6.1. Regras de Domínio

1. **Separação Rígida de IA:** O LLM não executa comandos nem acessa a base de dados. Ele apenas transforma strings livres em instâncias tipadas de comandos (`ScheduleAppointmentCommand`, `CancelAppointmentCommand`).
2. **Disponibilidade Real:** Um agendamento só é aceito se existir slot livre na grade do médico requisitado.
3. **Prevenção de Colisão Concorrente (*Double Booking*):**
   - A reserva de slot deve operar sob transação determinística com trava pessimista ou constraint de banco única `(doctor_id, start_time, status=CONFIRMED)`.
4. **Imutabilidade do Histórico:** Conversas e transações executadas geram registros auditáveis.
5. **Idioma Adaptável:** A mensagem gerada pelo `messageGeneratorNode` deve sempre manter o idioma identificado na mensagem de entrada do paciente.

## 6.2. Estrutura do Estado Compartilhado (`ScheduleState`)

```java
public record ScheduleState(
    String conversationId,
    String patientId,
    String rawInputMessage,
    String detectedLanguage,
    IntentType intent,               // SCHEDULE, CANCEL, UNKNOWN
    ScheduleCommand command,         // DTO estruturado extraído pelo LLM
    DomainActionResult actionResult, // Resultado da execução determinística de domínio
    boolean actionSuccess,           // Sucesso da ação transacional
    String humanizedResponse         // Resposta final amigável ao paciente
) {}
```

## 6.3. Nós do Grafo (DAG)

1. `identifyIntentNode`: Chama a LLM com Structured Output para classificar a intenção e extrair os parâmetros rígidos.
2. `schedulerNode`: Executa a verificação determinística de horários e reserva a consulta médica.
3. `cancelNode`: Executa o cancelamento determinístico da consulta médica.
4. `fallbackNode`: Trata requisições incompreendidas ou fora do escopo médico.
5. `messageGeneratorNode`: Gera a resposta humanizada contextualizada com base no `actionResult`.

## 6.4. Diagrama Visual Manual da Topologia do Grafo (DAG)

A conexão entre os nós do grafo é orquestrada pela classe `ScheduleGraphOrchestrator`. A topologia representa um grafo acíclico dirigido (DAG) com bifurcação condicional baseada na intenção extraída e convergência unificada para a geração de mensagem humanizada:

```text
                  +-----------------------------------+
                  |               INÍCIO              |
                  |     (ScheduleState inicial)       |
                  +-----------------+-----------------+
                                    |
                                    v
                  +-----------------------------------+
                  |         identifyIntent            |
                  |  - Chama Spring AI / OpenRouter   |
                  |  - Extrai IntentType & Parâmetros |
                  |  - Detecta idioma do paciente     |
                  +-----------------+-----------------+
                                    |
                                    v
                         /---------------------\
                        <  state.intent() == ?  >
                         \---------------------/
                                    |
            +-----------------------+-----------------------+
            | SCHEDULE              | CANCEL                | UNKNOWN (outros)
            v                       v                       v
  +-------------------+   +-------------------+   +-------------------+
  |     scheduler     |   |      cancel       |   |     fallback      |
  | - Valida Doctor   |   | - Valida Consulta |   | - Trata mensagem  |
  | - Checa conflito  |   | - Cancela no BD   |   |   ambígua ou fora |
  | - Salva no BD     |   | - Libera horário  |   |   de escopo       |
  +---------+---------+   +---------+---------+   +---------+---------+
            |                       |                       |
            +-----------------------+-----------------------+
                                    |
                                    v
                  +-----------------------------------+
                  |         messageGenerator          |
                  |  - Gera mensagem amigável         |
                  |  - Respeita idioma (pt-BR, etc.)  |
                  |  - Comunica o actionResult        |
                  +-----------------+-----------------+
                                    |
                                    v
                  +-----------------------------------+
                  |                FIM                |
                  |         (StateGraph.END)          |
                  +-----------------------------------+
```

## 6.5. Explicação do Método `process` do `ScheduleGraphOrchestrator`

O método `process` é o ponto de entrada único (*Single Entry Point*) que conecta a camada de controle HTTP (REST Controller) à máquina de estados determinística:

```java
public ScheduleState process(String conversationId, String patientId, String message) {
    ScheduleState initial = ScheduleState.initial(conversationId, patientId, message);

    return graph.execute(initial);
}
```

### Ciclo de Execução Passo a Passo:

1. **Instanciação do Estado Inicial Imutável:**
   - Invoca a factory `ScheduleState.initial(conversationId, patientId, message)`.
   - Cria uma nova instância de `ScheduleState` (Java 21 `record`) com campos obrigatórios preenchidos e campos operacionais nulos ou com valores padrão (`intent = UNKNOWN`, `actionSuccess = false`).
2. **Disparo da Máquina de Estados (`graph.execute(initial)`):**
   - O `StateGraph` recebe o nó inicial registrado (`identifyIntent`).
   - O orquestrador executa o nó inicial, que aciona a LLM e retorna um novo `ScheduleState` com intenção, idioma e parâmetros extraídos.
3. **Avaliação da Aresta Condicional (`addConditionalEdge`):**
   - O grafo avalia a função lambda registrada para `identifyIntent`:
     - Se `state.intent() == IntentType.SCHEDULE` $\rightarrow$ direciona para o nó `"scheduler"`.
     - Se `state.intent() == IntentType.CANCEL` $\rightarrow$ direciona para o nó `"cancel"`.
     - Caso contrário $\rightarrow$ direciona para o nó `"fallback"`.
4. **Execução Determinística do Nó de Negócio:**
   - O nó selecionado executa sua operação de forma atômica e atualiza os campos `actionResult` e `actionSuccess` via derivação imutável (`state.withActionResult(...)`).
5. **Convergência Determinística para `messageGenerator`:**
   - Independentemente do ramo percorrido (agendamento, cancelamento ou fallback), todas as arestas convergem para o nó `"messageGenerator"`.
   - O gerador sintetiza a resposta final no campo `humanizedResponse`.
6. **Finalização (`StateGraph.END`):**
   - Ao atingir a constante `StateGraph.END`, o laço de execução do grafo termina e o `ScheduleState` final, completo e imutável, é retornado ao controller para serialização no response JSON da API.

---

## 6.6. Conexão ao Banco de Dados e o Papel do Flyway

A persistência do sistema é construída sobre o padrão **Clean Architecture / Hexagonal**, garantindo que o domínio nunca dependa diretamente de drivers de banco ou de anotações JPA.

### 1. Camada de Conexão e Pool de Conexões (HikariCP + Spring Data JPA)
- **Pool de Conexões:** O Spring Boot inicializa automaticamente o pool de alta performance **HikariCP**, gerenciando conexões ativas e reutilizáveis.
- **Isolamento por Perfil:**
  - **Perfil `dev` / `test`:** Conecta-se a um banco de dados em memória **H2** (`jdbc:h2:mem:schedulerdev`), permitindo execução rápida e isolada sem necessidade de containers externos.
  - **Perfil padrão / produção:** Conecta-se ao **PostgreSQL 16** via JDBC (`jdbc:postgresql://localhost:5432/schedulerdb`), provisionado via Docker Compose.
- **Validação de Schema com Hibernate:**
  - Está configurado `spring.jpa.hibernate.ddl-auto: validate`.
  - Isso significa que o Hibernate **NÃO** altera nem cria tabelas em tempo de execução. Ele apenas valida se as classes mapeadas com `@Entity` coincidem estritamente com as tabelas reais do banco.

### 2. O Papel Central do Flyway no Ciclo de Vida da Aplicação
O **Flyway** é o componente responsável pelo versionamento determinístico de banco de dados (*Database Migrations / Database as Code*).

#### Como o Flyway opera na inicialização:
1. **Ordem de Inicialização Precedente:**
   - Durante o bootstrap do Spring Boot, o Flyway intercepta o `DataSource` **antes** que o Hibernate inicialize o `EntityManagerFactory`.
2. **Tabela de Controle (`flyway_schema_history`):**
   - O Flyway consulta ou cria a tabela interna `flyway_schema_history` no banco para saber exatamente quais migrações já foram aplicadas.
3. **Execução Sequencial e Transacional de Scripts SQL:**
   - Lê os arquivos no diretório `src/main/resources/db/migration/`:
     - `V1__create_tables.sql`: Executa a criação das tabelas `doctors` e `appointments`, incluindo chaves primárias, constraints relacionais e índices.
     - `V2__seed_doctors.sql`: Insere a carga de dados inicial determinística de médicos (`Dr. Alício`, `Dr. Roberto`, `Dra. Beatriz Santos`).
   - Cada script é executado dentro de uma transação. Se houver falha, ocorre rollback automático da migração e a aplicação é abortada antes de aceitar tráfego.
4. **Benefícios Arquiteturais:**
   - **Reprodutibilidade Total:** O mesmo schema e dados de referência sobem identicamente em ambientes locais, testes de integração (Testcontainers) e produção.
   - **Auditoria e Rastreabilidade:** Qualquer alteração no schema relacional é versionada no Git via scripts SQL versionados (`V1`, `V2`, `V3...`), impedindo divergências estruturais entre código e banco.

---

# 7. Code Quality Standards

## 7.1. Princípios
- **SOLID, KISS e DRY.**
- **Simple Design:** Começar sempre pela implementação mais simples que faz o teste passar.
- **Fail Fast:** Validação imediata de entradas e schemas.
- **Explicit over Implicit:** Roteamento e transições do grafo explícitos em código.

## 7.2. Tratamento de Erros
- Erros de validação de schema do LLM direcionam para o `fallbackNode` com causa registrada, sem lançar exceções não tratadas ao cliente HTTP.
- Respostas da API REST seguem o padrão RFC 7807 (Problem Details for HTTP APIs) para erros HTTP 4xx/5xx.

---

# 8. Testing Strategy

## 8.1. Estrutura de Testes

| Tipo | Objetivo | Ferramentas |
|---|---|---|
| Unitários (Domínio) | Validar regras de agendamento, conflito e horários isoladamente | JUnit 5, AssertJ |
| Unitários (Grafo) | Validar transições de estados e execução dos nós com mocks | JUnit 5, Mockito |
| Arquitetura | Garantir isolamento de pacotes e contrato de nós do Grafo | ArchUnit |
| Integração (DB) | Validar persistência e concorrência simultânea | Testcontainers (PostgreSQL) |
| E2E (REST API) | Validar fluxo completo desde a requisição HTTP | MockMvc / WebTestClient |

## 8.2. Test-Driven Development (TDD)
- **Red:** Escrever o teste para o nó ou serviço de domínio antes de escrever o código.
- **Green:** Implementar o código mínimo necessário para o teste passar.
- **Refactor:** Eliminar duplicações e aperfeiçoar design mantendo testes verdes.

---

# 9. AI Pair Programming — Extreme Programming

Adotamos Pair Programming estrito entre o Usuário e o Agente de IA:
- O agente não assume requisitos não especificados.
- O agente divide tarefas em pequenos incrementos funcionais e testáveis (Small Releases).
- O agente explica os passos e aguarda aprovação antes de avançar entre etapas maiores.

---

# 10. Documentation Governance

## 10.1. Documentos Complementares

| Documento | Responsabilidade |
|---|---|
| `docs/AGENTS.md` | Fonte central da verdade técnica e governança |
| `docs/architecture/decisions/ADR-001-state-graph-orchestration-and-stack.md` | Registro formal das decisões de arquitetura e stack |
| `docs/requirements/requirements.md` | Especificação detalhada de requisitos funcionais e não funcionais |
| `docs/development/development-plan.md` | Plano incremental de entregas (Milestones e Sprints) |
| `docs/development/progress.md` | Estado atual do planejamento e das entregas |
| `docs/development/development-log.md` | Diário de bordo cronológico de ocorrências e alterações |

---

# 11. Definition of Done (DoD)

Uma tarefa somente pode ser considerada concluída quando:
- [ ] Requisitos e critérios de aceitação validados.
- [ ] Testes unitários/integração implementados e passando (TDD).
- [ ] Código revisado respeitando a arquitetura hexagonal e isolamento de camadas.
- [ ] Nenhuma violação em testes ArchUnit.
- [ ] `progress.md` e `development-log.md` atualizados.

---

# 12. Initial Instructions for the AI Agent

Ao iniciar uma nova sessão:
1. Leia este documento e os documentos de `docs/development/`.
2. Verifique o estado atual do código e dos testes.
3. Nunca avance para o próximo incremento sem alinhar os critérios de aceitação com o desenvolvedor.