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
- **Java 21** (ou utilize o script `./mvnw` incluso que gerencia automaticamente).
- **Docker e Docker Compose** (necessário *apenas* se optar por rodar com PostgreSQL).
- Chave de API da [OpenRouter](https://openrouter.ai/) para processar linguagem natural via LLM.

---

### Opção A: Modo Desenvolvimento Rápido (Sem Docker / Banco H2 In-Memory)

Ideal para desenvolvimento ágil e testes imediatos, **sem necessidade de subir containers ou instalar o PostgreSQL**:
- O perfil `dev` já está **ativo por padrão** em `application.yml`.
- Utiliza banco relacional **H2 em memória** (`jdbc:h2:mem:schedulerdev`).
- Os médicos iniciais (`Dr. Alício`, `Dr. Roberto`, `Dra. Beatriz Santos`) são populados automaticamente pelo `DataInitializer`.

#### 1. Configure a chave da OpenRouter e inicie a aplicação:
```bash
export OPENROUTER_API_KEY="sua_chave_aqui"
./mvnw spring-boot:run
```

A API estará pronta em `http://localhost:8080`.

---

### Opção B: Modo Produção / Staging (Com PostgreSQL via Docker Compose)

Utilizado quando você deseja persistência duradoura em disco e validação do ciclo de migrações com o **Flyway**:

#### 1. Inicie o container PostgreSQL:
```bash
docker compose up -d
```

#### 2. Execute a aplicação ativando o perfil de produção:
```bash
export OPENROUTER_API_KEY="sua_chave_aqui"
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

O Flyway criará automaticamente o schema (`V1__create_tables.sql`) e fará a carga inicial de dados (`V2__seed_doctors.sql`).

#### 3. Para encerrar e excluir o container do PostgreSQL:
```bash
# Para apenas parar e remover o container:
docker compose down

# Para parar, remover e apagar todo o volume de dados do banco:
docker compose down -v
```

#### 💡 Como a Conexão Funciona com o Docker Compose

Quando o banco é iniciado via `docker compose up -d`, a integração ocorre através das seguintes etapas:

```text
+------------------------------------+           +-----------------------------------------+
|     Aplicação Spring Boot          |           |      Docker Container                   |
|     (Executando no Host)           |           |      (scheduler-postgres)               |
|                                    |           |                                         |
|  HikariCP Connection Pool          |           |  PostgreSQL 16 Alpine                   |
|  jdbc:postgresql://localhost:5432/ |  TCP:5432 |  Database: schedulerdb                  |
|  User: scheduler                   | --------> |  Porta interna: 5432                    |
|  Pass: scheduler_secret            |           |  User: scheduler                        |
+------------------------------------+           +--------------------+--------------------+
                                                                      |
                                                                      v
                                                         +-------------------------+
                                                         |  Volume Docker Nomeado  |
                                                         |  (postgres_data)        |
                                                         |  /var/lib/postgresql/   |
                                                         |  data                   |
                                                         +-------------------------+
```

1. **Encaminhamento de Portas (*Port Forwarding*):**
   - No `docker-compose.yml`, a instrução `ports: ["5432:5432"]` vincula a porta interna `5432` do container à porta `5432` do seu computador (*localhost*).
2. **Conexão JDBC da Aplicação:**
   - A aplicação Spring Boot roda na máquina host e utiliza a URL padrão configurada em `application.yml`:
     `jdbc:postgresql://${POSTGRES_HOST:localhost}:${POSTGRES_PORT:5432}/${POSTGRES_DB:schedulerdb}`.
   - O pool de conexões **HikariCP** estabelece a conexão TCP via socket na porta `5432` com usuário `scheduler` e senha `scheduler_secret`.
3. **Healthcheck e Disponibilidade:**
   - O container possui um healthcheck contínuo (`pg_isready -U scheduler -d schedulerdb`), garantindo que o PostgreSQL esteja pronto para aceitar conexões e responder às consultas da aplicação.
4. **Ciclo de Inicialização do Flyway:**
   - Assim que a conexão com o PostgreSQL é confirmada, o **Flyway** conecta na base `schedulerdb`, cria a tabela `flyway_schema_history` e aplica as migrações SQL sequenciais (`V1` de tabelas e `V2` de médicos).
5. **Persistência em Disco via Volume (`postgres_data`):**
   - O diretório `/var/lib/postgresql/data` do container é mapeado para o volume gerenciado `postgres_data`. Isso garante que, se você reiniciar o computador ou derrubar o container com `docker compose down`, seus agendamentos continuam salvos e não serão perdidos.

---

### 🧪 Executar a Suíte de Testes Automatizados (TDD & ArchUnit)

Para executar todos os testes unitários, testes de arquitetura (ArchUnit) e de integração:
```bash
./mvnw clean test
```

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
