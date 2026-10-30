# 01 — Visão geral (AS-IS)

> Documentação de engenharia reversa do `trabalhista-ai`. Descreve o que o código faz **hoje**, sem propor mudanças como se já existissem.
> Data da análise: 2026-10-29. Base: árvore de trabalho da branch `main` (HEAD `63d9760`, mais alterações ainda sem commit).

## 1. O que o sistema é

Aplicação Spring Boot (API REST) com dois fluxos de IA sobre o mesmo `ChatClient` do Spring AI:

1. **Atendimento trabalhista com RAG.** Recebe uma pergunta, busca trechos num banco vetorial (PostgreSQL com pgvector), monta um prompt com o contexto e pede ao modelo (OpenAI) uma resposta estruturada (`AtendimentoResultado`). A conversa é guardada numa memória em MongoDB.
2. **Ação de agente com aprovação humana (Human in the Loop).** Recebe um comando livre. O modelo pode chamar a tool `solicitarEnvioEmail`, que só grava uma solicitação `PENDENTE`. Uma pessoa aprova ou rejeita por outro endpoint, e só a aprovação envia o e-mail por SMTP.

Há também ingestão e busca de conhecimento (`/api/conhecimentos`) e auditoria de execuções com tokens, duração e custo estimado (`/api/auditoria`).

O propósito acima é **inferido** do código, dos nomes e do prompt de sistema. Não existe README nem spec no repositório (`HELP.md` é o do Spring Initializr). O domínio "trabalhista" aparece em nomes, no prompt de `AtendimentoService` e no metadado `dominio` dos chunks.

## 2. Stack observada

| Item | Evidência |
|---|---|
| Java 21 | `pom.xml:22` |
| Spring Boot 3.5.7 | `pom.xml:13` |
| Spring AI 1.1.8 (BOM) | `pom.xml:23` |
| Web, Validation, Data JPA, Data MongoDB, Mail, Actuator | `pom.xml:40-95` |
| Modelo de chat e embedding: OpenAI | `spring-ai-starter-model-openai`, `application.yaml:38-47` |
| Vector store: pgvector | `spring-ai-starter-vector-store-pgvector`, `application.yaml:49-54` |
| Memória conversacional: MongoDB | `spring-ai-starter-model-chat-memory-repository-mongodb`, `AiConfig.java:15-23` |
| Documentação OpenAPI: springdoc 2.10.0 | `pom.xml:86-90` |
| Banco relacional: PostgreSQL | driver em `pom.xml:60-64` |

**Dependências que não existem no `pom.xml`:** Spring Security, Flyway/Liquibase, cliente ou servidor MCP, A2A, Micrometer Tracing/OpenTelemetry, Testcontainers, Mockito além do que vem em `spring-boot-starter-test`.

## 3. Escopo e legenda

Estados usados na matriz (`13-matriz-rastreabilidade.md`):

| Estado | Significado |
|---|---|
| IMPLEMENTADO | Há código que realiza a funcionalidade. Isso **não** implica que tenha sido executado ou testado. |
| PARCIAL | Há código, mas incompleto em relação ao que o nome ou a intenção sugerem. |
| NÃO ENCONTRADO | Nada no repositório realiza a funcionalidade. |
| NÃO VERIFICADO | Depende de execução, de configuração externa ou de comportamento padrão da biblioteca que não foi conferido. |

Diferenciação usada nos documentos: **código** (classes e métodos), **configuração** (`application.yaml`, `docker-compose.yml`), **teste** (somente `contextLoads`) e **intenção documentada** (comentários e descrições de tool/prompt no código).

## 4. Estado do repositório

- O único commit (`63d9760`) contém só o esqueleto do Spring Initializr e um `application.yaml` de 3 linhas.
- Todo o código descrito aqui está **sem commit**: `config/`, `controllers/`, `dtos/`, `entities/`, `repositories/`, `services/`, `tools/`, `docker-compose.yml`, mais a alteração do `application.yaml`, `.claude/` e `.specify/`.
- O GitHub Spec Kit já foi instalado (`.specify/`, `.claude/skills/speckit-*`), mas `.specify/memory/constitution.md` ainda é o **template sem preencher** (placeholders `[PRINCIPLE_1_NAME]` etc.).

## 5. Limites desta análise

- Não foi subida a aplicação, os containers nem o teste `contextLoads`. Comportamentos em execução (códigos HTTP reais, nomes de tabela e coleção criados pelas bibliotecas, valores padrão de `topK`) constam como NÃO VERIFICADO quando não estão no repositório.
- O arquivo `.env` existe na raiz e está no `.gitignore`. Não foi lido, e nenhum segredo é reproduzido nestes documentos.
- Preços de tokens em `application.yaml` não foram conferidos com a tabela oficial do provedor.

## 6. Índice

| Arquivo | Assunto |
|---|---|
| `02-arquitetura.md` | Camadas, diagrama geral, desvios |
| `03-componentes.md` | Classes, responsabilidades, dependências |
| `04-contratos-api.md` | Endpoints, DTOs, validações, erros |
| `05-modelo-dados.md` | Tabelas, vetor, memória, ER |
| `06-fluxos-negocio.md` | Fluxos e sequência com memória |
| `07-integracoes-ia.md` | OpenAI, RAG, tools, HITL, MCP/agentes |
| `08-infraestrutura.md` | Docker Compose e configuração |
| `09-seguranca.md` | Achados de segurança e LGPD |
| `10-observabilidade.md` | Auditoria, custos, tracing |
| `11-testes.md` | Testes existentes e lacunas |
| `12-dividas-tecnicas.md` | Dívidas e inconsistências |
| `13-matriz-rastreabilidade.md` | Funcionalidade → evidência → estado |
