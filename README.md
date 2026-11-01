# Sistema de Agendamento Médico Transacional com Structured Outputs

> Microsserviço corporativo em Java 21 e Spring Boot 3.4+ que traduz texto livre de pacientes em ações transacionais determinísticas de agendamento e cancelamento de consultas médicas.

---

## 🎯 Visão do Produto & Princípios

- **Separação Rígida de Responsabilidades (IA vs. Domínio):** O modelo de linguagem (LLM) **nunca** executa lógica de negócio, regras de calendário ou persistência diretamente. Ele atua estritamente como um tradutor de linguagem natural para contratos JSON rígidos (*Structured Outputs*) via schema tipado.
- **Orquestração com Máquina de Estados (DAG):** Grafo direcionado acíclico com nós explícitos (`identifyIntent`, `schedulerNode`, `cancelNode`, `fallbackNode`, `messageGeneratorNode`) e estado imutável compartilhado (`ScheduleState`).
- **Lógica Determinística de Domínio:** Verificação real de disponibilidade, tratamento de conflitos simultâneos de agenda (*anti-double booking*) e resposta humanizada adaptada ao idioma do usuário.
- **Governança Automatizada com ArchUnit:** Testes de arquitetura que garantem o isolamento das camadas e a padronização dos nós do grafo.

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| **Linguagem** | Java 21 LTS |
| **Framework Base** | Spring Boot 3.4.2 |
| **IA & LLM** | Spring AI (OpenAI Chat Client apontando para OpenRouter) |
| **Persistência** | PostgreSQL 16 (produção/local via Docker Compose) e H2 (in-memory nos testes) |
| **Migrações de Banco** | Flyway 10.x |
| **Testes & TDD** | JUnit 5, AssertJ, Mockito, Spring MockMvc |
| **Governança Arquitetural** | ArchUnit 1.3 |
| **Containerização** | Docker & Docker Compose |

---

## 📂 Estrutura Arquitetural (Clean Architecture)

```text
src/main/java/com/med/scheduler/
├── domain/                               # Núcleo de negócio puro (Java 21 sem frameworks)
│   ├── model/                            # Doctor, Appointment, AppointmentStatus
│   ├── repository/                       # DoctorRepositoryPort, AppointmentRepositoryPort
│   └── service/                          # AppointmentDomainService (regras determinísticas)
├── graph/                                # Engine e Nós da Máquina de Estados (DAG)
│   ├── core/                             # Node, EdgeCondition, StateGraph
│   ├── state/                            # ScheduleState (Record imutável), IntentType
│   ├── nodes/                            # IdentifyIntentNode, SchedulerNode, CancelNode, FallbackNode, MessageGeneratorNode
│   └── ScheduleGraphOrchestrator.java    # Orquestrador do DAG completo
└── infrastructure/                       # I/O, Frameworks e Adaptadores
    ├── ai/                               # Spring AI ChatClient, DTOs de Structured Output, OpenRouter service
    ├── persistence/                      # Entidades JPA, Spring Data Repositories, Adaptadores Flyway
    ├── config/                           # Configurações Spring Beans
    └── web/                              # Controller REST e DTOs de Request/Response
```

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- JDK 21 instalado (ou use o Maven Wrapper incluído).
- Docker e Docker Compose instalados.

### 1. Iniciar o Banco de Dados (PostgreSQL) via Docker Compose

```bash
docker compose up -d
```

### 2. Configurar a Chave do OpenRouter (Opcional para ambiente real)

Defina a variável de ambiente com sua chave da OpenRouter:
```bash
export OPENROUTER_API_KEY="sua_chave_aqui"
```

> **Nota:** Para desenvolvimento local rápido, a aplicação já possui fallback configurado no perfil `dev` com banco H2 in-memory caso o PostgreSQL não esteja rodando.

### 3. Executar os Testes Automatizados (TDD & ArchUnit)

```bash
./mvnw clean test
```

### 4. Iniciar a Aplicação

```bash
./mvnw spring-boot:run
```

A API estará disponível em `http://localhost:8080`.

---

## 📡 Como Consumir a API REST

### Endpoint: `POST /api/v1/chat/schedule`

#### Exemplo 1: Solicitação de Agendamento
```bash
curl -X POST http://localhost:8080/api/v1/chat/schedule \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": "conv-101",
    "patientId": "paciente-789",
    "message": "Olá! Gostaria de agendar uma consulta com o Dr. Roberto amanhã às 14:00."
  }'
```

**Exemplo de Resposta:**
```json
{
  "conversationId": "conv-101",
  "patientId": "paciente-789",
  "intent": "SCHEDULE",
  "actionSuccess": true,
  "actionResult": "Consulta confirmada com sucesso: Dr. Roberto em 2026-11-02 às 14:00 (Especialidade: Ortopedia).",
  "humanizedResponse": "Olá! Consulta confirmada com sucesso: Dr. Roberto em 2026-11-02 às 14:00 (Especialidade: Ortopedia). Aguardamos você."
}
```

#### Exemplo 2: Solicitação de Cancelamento
```bash
curl -X POST http://localhost:8080/api/v1/chat/schedule \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": "conv-102",
    "patientId": "paciente-789",
    "message": "Preciso cancelar minha consulta de id app-12345."
  }'
```

#### Exemplo 3: Mensagem Fora de Escopo (Fallback)
```bash
curl -X POST http://localhost:8080/api/v1/chat/schedule \
  -H "Content-Type: application/json" \
  -d '{
    "conversationId": "conv-103",
    "patientId": "paciente-789",
    "message": "Qual é a previsão do tempo para o fim de semana?"
  }'
```

---

## 📚 Documentação Técnica de Governança

- [AGENTS.md](file:///home/user/%C3%81rea%20de%20trabalho/temp/Projetos/Sistema-de-Agendamento-M-dico-Transacional-com-Structured-Outputs/docs/AGENTS.md): Diretrizes centrais de governança e colaboração técnica com agentes de IA.
- [ADR-001](file:///home/user/%C3%81rea%20de%20trabalho/temp/Projetos/Sistema-de-Agendamento-M-dico-Transacional-com-Structured-Outputs/docs/architecture/decisions/ADR-001-state-graph-orchestration-and-stack.md): Registro da decisão arquitetural (Spring Boot 3.4+, Java 21, Spring AI via OpenRouter, Engine de Grafo interna e Docker).
- [requirements.md](file:///home/user/%C3%81rea%20de%20trabalho/temp/Projetos/Sistema-de-Agendamento-M-dico-Transacional-com-Structured-Outputs/docs/requirements/requirements.md): Requisitos funcionais e não funcionais.
- [progress.md](file:///home/user/%C3%81rea%20de%20trabalho/temp/Projetos/Sistema-de-Agendamento-M-dico-Transacional-com-Structured-Outputs/docs/development/progress.md): Acompanhamento das entregas e marcos do projeto.
- [development-log.md](file:///home/user/%C3%81rea%20de%20trabalho/temp/Projetos/Sistema-de-Agendamento-M-dico-Transacional-com-Structured-Outputs/docs/development/development-log.md): Diário de bordo cronológico de todas as implementações.
