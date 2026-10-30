# 13 — Matriz de rastreabilidade (AS-IS)

Estados: **IMPLEMENTADO**, **PARCIAL**, **NÃO ENCONTRADO**, **NÃO VERIFICADO** (definições em `01-visao-geral.md`).
Em "Testes", `—` significa que nenhum teste cobre a funcionalidade. O único teste do projeto é `contextLoads` (`11-testes.md`), que não conta como cobertura funcional.
Caminho base das classes: `br.com.cotiinformatica.trabalhista_ai`. Referências `Classe.método:linhas`.

## 1. Funcionalidades

| ID | Descrição | Evidência no código | Endpoint | Dados utilizados | Integrações externas | Testes | Estado |
|---|---|---|---|---|---|---|---|
| F01 | Atendimento trabalhista com RAG e resposta estruturada | `AtendimentoService.executar:34-126`; `AtendimentoController.executar:23-28` | `POST /api/atendimentos` | `PerguntaRequest`, trechos recuperados, `AtendimentoResultado` | OpenAI (chat e embedding), pgvector | — | IMPLEMENTADO (execução NÃO VERIFICADA) |
| F02 | Validação de entrada do atendimento | `PerguntaRequest` (`@NotBlank`), `@Valid` em `AtendimentoController:25` | `POST /api/atendimentos` | `usuarioId`, `conversaId`, `pergunta` | — | — | PARCIAL (sem limite de tamanho) |
| F03 | Saída estruturada por schema | `AtendimentoService:44-45,111-113`; `AtendimentoResultado` | `POST /api/atendimentos` | JSON do modelo | OpenAI | — | PARCIAL (sem validação semântica nem retry) |
| F04 | Ingestão de conhecimento com chunking simples | `ConhecimentoService.cadastrar:19-44`; `ConhecimentoController.cadastrar:27-37` | `POST /api/conhecimentos` | `texto`, `fonte`, metadado `dominio` | OpenAI (embedding), pgvector | — | IMPLEMENTADO (chunking provisório) |
| F05 | Busca semântica de trechos | `ConhecimentoService.pesquisar:46-48`; `ConhecimentoController.pesquisar:39-46` | `GET /api/conhecimentos/pesquisar` | `pergunta`, texto dos trechos | OpenAI (embedding), pgvector | — | IMPLEMENTADO (`topK` e limiar NÃO VERIFICADOS) |
| F06 | Citação de fontes na resposta | contexto com `Fonte:` (`AtendimentoService:39-42`); regra de prompt (`:60`); campo `fontes` | `POST /api/atendimentos` | metadado `fonte` | — | — | PARCIAL (só por prompt, sem verificação) |
| F07 | Recusa quando não há informação | regra de prompt (`:58-59`), `informacaoEncontrada` | `POST /api/atendimentos` | contexto vazio | OpenAI | — | PARCIAL (só por prompt) |
| F08 | Memória conversacional por janela de 10 mensagens | `AiConfig.chatMemory:16-23`, `AiConfig.chatClient:26-36`; `CONVERSATION_ID` em `AtendimentoService:89-92` | `POST /api/atendimentos` | histórico no MongoDB | MongoDB | — | IMPLEMENTADO (conteúdo gravado NÃO VERIFICADO) |
| F09 | Function Calling: solicitar envio de e-mail | `EmailTools.solicitarEnvioEmail:19-46`; `.tools(emailTools)` em `AcaoAgenteController:53` | `POST /api/agente/acoes` | `comando`, `conversaId`; `SolicitacaoEmail` | OpenAI | — | IMPLEMENTADO |
| F10 | Ação do agente com prompt de RH e memória | `AcaoAgenteController.executar:29-62` | `POST /api/agente/acoes` | `comando`, `conversaId` | OpenAI, MongoDB | — | PARCIAL (sem DTO, sem auditoria, sem usuário) |
| F11 | Listagem de solicitações pendentes | `EmailService.pendentes:31-33`; `AprovacaoController.pendentes:25-28` | `GET /api/aprovacoes/pendentes` | `SolicitacaoEmail` | PostgreSQL | — | IMPLEMENTADO |
| F12 | Aprovação humana e envio de e-mail (HITL) | `EmailService.aprovar:35-60`; `AprovacaoController.aprovar:30-36` | `POST /api/aprovacoes/{id}/aprovar` | `id`, `responsavel`, `SolicitacaoEmail` | SMTP (MailHog em dev), PostgreSQL | — | IMPLEMENTADO (sem identidade verificada; envio SMTP NÃO VERIFICADO) |
| F13 | Rejeição humana | `EmailService.rejeitar:62-78`; `AprovacaoController.rejeitar:38-44` | `POST /api/aprovacoes/{id}/rejeitar` | `id`, `responsavel` | PostgreSQL | — | PARCIAL (sem justificativa) |
| F14 | Controle de concorrência na decisão | `synchronized` (`EmailService:35,62`); `@Version` (`SolicitacaoEmail:39-40`) | aprovar/rejeitar | `versao`, `status` | PostgreSQL | — | PARCIAL (válido numa instância; comportamento do `@Version` NÃO VERIFICADO) |
| F15 | Auditoria de execução: tokens, modelo, duração, status | `AuditoriaService.registrar:28-81`, `registrarErro:83-98`; `ExecucaoAgente` | usado por `POST /api/atendimentos` | `ExecucaoAgente` | PostgreSQL | — | IMPLEMENTADO (só atendimento; duplicidade em falha de conversão) |
| F16 | Cálculo de custo estimado em USD | `AuditoriaService:62-76`; `app.pricing.*` | `POST /api/atendimentos` (efeito) | tokens, preços do YAML | — | — | IMPLEMENTADO (preços NÃO VERIFICADOS) |
| F17 | Consulta de auditoria por usuário | `AuditoriaController.consultar:24-29`; `ExecucaoAgenteRepository.findByUsuarioId` | `GET /api/auditoria/usuario/{usuarioId}` | `ExecucaoAgente` | PostgreSQL | — | IMPLEMENTADO (sem autorização) |
| F18 | Health, info e métricas do actuator | `application.yaml:56-60` | `/actuator/health`, `/info`, `/metrics` | métricas da JVM e do Spring | — | — | NÃO VERIFICADO (configurado, não executado) |
| F19 | Documentação OpenAPI | dependência springdoc `pom.xml:86-90` | caminhos padrão do springdoc | contrato dos endpoints | — | — | NÃO VERIFICADO (só dependência) |
| F20 | Persistência relacional com criação automática de esquema | `ddl-auto: update`; 2 entidades | — | `solicitacoes_email`, `execucoes_agente` | PostgreSQL | `contextLoads` (indireto) | IMPLEMENTADO (execução NÃO VERIFICADA) |
| F21 | Esquema vetorial HNSW, cosseno, 1536 | `application.yaml:49-54` | — | índice vetorial | pgvector | `contextLoads` (indireto) | IMPLEMENTADO (configuração; nomes reais NÃO VERIFICADOS) |
| F22 | Infraestrutura local por Docker Compose | `docker-compose.yml` | — | — | Postgres, Mongo, pgAdmin, mongo-express, MailHog | — | IMPLEMENTADO (configuração; não executada) |
| F23 | Contexto da aplicação sobe | `TrabalhistaAiApplicationTests.contextLoads` | — | — | Postgres, Mongo, chave OpenAI | `contextLoads` | NÃO VERIFICADO (não executado) |

## 2. Funcionalidades esperadas e não encontradas

| ID | Descrição | Resultado da busca | Estado |
|---|---|---|---|
| N01 | Servidor ou cliente MCP | nenhuma dependência, anotação ou classe | NÃO ENCONTRADO |
| N02 | Protocolo A2A | nenhuma dependência ou classe | NÃO ENCONTRADO |
| N03 | Integração REST entre agentes | nenhum `RestClient`, `RestTemplate` ou `WebClient`; dois fluxos no mesmo processo | NÃO ENCONTRADO |
| N04 | Delegação ou orquestração entre agentes | sem roteador nem segundo agente | NÃO ENCONTRADO |
| N05 | Tracing distribuído e id de correlação | sem Micrometer Tracing/OTel | NÃO ENCONTRADO |
| N06 | Autenticação e autorização | sem Spring Security | NÃO ENCONTRADO |
| N07 | Remoção de documento do conhecimento | `ConhecimentoService` só tem `cadastrar` e `pesquisar` | NÃO ENCONTRADO |
| N08 | Filtro por dono/tenant na busca vetorial | nenhuma expressão de filtro | NÃO ENCONTRADO |
| N09 | Eval e testes de comportamento do modelo | sem casos de eval nem dublê do modelo | NÃO ENCONTRADO |
| N10 | Retenção e exclusão de dados (LGPD) | sem rotina nem endpoint | NÃO ENCONTRADO |
| N11 | Limites de consumo (tokens, taxa, orçamento) | sem configuração | NÃO ENCONTRADO |
| N12 | Mascaramento ou pseudonimização antes do envio ao provedor | sem código | NÃO ENCONTRADO |
| N13 | Migrações de banco versionadas | sem Flyway/Liquibase | NÃO ENCONTRADO |
| N14 | Tratamento global de erros (ProblemDetail) | sem `@ControllerAdvice` | NÃO ENCONTRADO |
| N15 | Constituição e specs do Spec Kit preenchidas | constituição é o template | NÃO ENCONTRADO |

## 3. Rastreabilidade por endpoint

| Endpoint | Funcionalidades |
|---|---|
| `POST /api/atendimentos` | F01, F02, F03, F06, F07, F08, F15, F16 |
| `POST /api/conhecimentos` | F04 |
| `GET /api/conhecimentos/pesquisar` | F05 |
| `POST /api/agente/acoes` | F08 (memória), F09, F10 |
| `GET /api/aprovacoes/pendentes` | F11 |
| `POST /api/aprovacoes/{id}/aprovar` | F12, F14 |
| `POST /api/aprovacoes/{id}/rejeitar` | F13, F14 |
| `GET /api/auditoria/usuario/{usuarioId}` | F17 |
| `/actuator/*` | F18 |

## 4. Pontos que precisam de confirmação humana

1. Os dois fluxos (atendimento e ação do agente) são o escopo final, ou MCP e multiagentes são intenção futura?
2. Os preços de `gpt-4.1-mini` em `application.yaml` estão corretos?
3. Quais são os valores padrão de `topK` e limiar do `similaritySearch(String)` na versão 1.1.8 do Spring AI (não conferidos)?
4. Haverá autenticação (e de que tipo) e como `usuarioId` e `responsavel` serão obtidos?
5. Existe contrato com a OpenAI que a coloque como operadora, sem treino, com retenção definida e base para transferência internacional?
6. Quais dados de processo e dados pessoais entrarão em perguntas, comandos e e-mails, e qual a retenção desejada?
7. A memória conversacional deve guardar o prompt completo com contexto, ou só a pergunta e a resposta?
8. O que a spec deve tratar como comportamento oficial e o que deve entrar como correção?
