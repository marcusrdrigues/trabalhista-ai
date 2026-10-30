# 09 — Segurança e LGPD (AS-IS)

Levantamento do que o código **faz ou deixa de fazer**, com a régua do OWASP Top 10 (web/API), OWASP LLM e LGPD. É um registro do estado atual, sem correções aplicadas. Segredos não são reproduzidos.

Severidade é uma avaliação qualitativa baseada só na leitura do código (ALTA / MÉDIA / BAIXA).

## 1. Controles presentes

| Controle | Evidência |
|---|---|
| Validação de entrada com `@Valid` + `@NotBlank` em atendimento e conhecimento | `AtendimentoController.java:25`, `ConhecimentoController.java:29`, DTOs |
| A tool de e-mail não envia; só cria registro `PENDENTE` | `EmailTools.java:19-46` |
| Envio só em `EmailService.aprovar`, com checagem de status `PENDENTE` | `EmailService.java:41-44` |
| Lock otimista na solicitação | `SolicitacaoEmail.java:39-40` (`@Version`) |
| Prompt de atendimento manda ignorar instruções vindas dos documentos | `AtendimentoService.java:57` (controle só por texto) |
| Acesso a banco por Spring Data (consultas derivadas, parâmetros vinculados) | `SolicitacaoEmailRepository`, `ExecucaoAgenteRepository` |
| `.env` ignorado no git | `.gitignore` |
| `health,info,metrics` como únicos endpoints do actuator expostos | `application.yaml:56-60` |

## 2. Achados

### Controle de acesso (OWASP A01 / API1, API2, API5)

| ID | Sev. | Achado | Evidência |
|---|---|---|---|
| S01 | ALTA | Nenhuma autenticação nem autorização em nenhum endpoint. Não há Spring Security. | `pom.xml`; todos os controllers |
| S02 | ALTA | `usuarioId` vem do cliente e é usado como chave de consulta: qualquer um lê a auditoria de qualquer usuário (IDOR). | `AuditoriaController.java:24-29` |
| S03 | ALTA | Qualquer chamador aprova ou rejeita qualquer solicitação, informando um `responsavel` livre. Disparar e-mail real é uma ação com efeito externo sem identidade verificada. | `AprovacaoController.java:30-44`, `EmailService.java:35-78` |
| S04 | ALTA | Qualquer chamador cadastra conhecimento. Esse conteúdo vira contexto do modelo (veneno de base). | `ConhecimentoController.java:27-37` |
| S05 | MÉDIA | Memória isolada só por chave de texto que o cliente escolhe. Em `/api/agente/acoes` a chave é `conversaId` puro, sem usuário. Um `conversaId` no formato `usuario:conversa` alcança a memória de um atendimento de outro usuário. | `AcaoAgenteController.java:54-57`, `AtendimentoService.java:89-92` |
| S06 | MÉDIA | `/api/agente/acoes` não identifica o usuário e a solicitação gerada não registra quem pediu. Sem rastreio de autoria da ação. | `AcaoAgenteController.java`, `SolicitacaoEmail.java` |

### Exposição de dados e erros (OWASP A02, A04, A05 / API3)

| ID | Sev. | Achado | Evidência |
|---|---|---|---|
| S07 | MÉDIA | Entidades JPA saem direto pela API, com todos os campos (inclusive `versao`, `erro`). | `AprovacaoController.java`, `AuditoriaController.java` |
| S08 | MÉDIA | `exception.getMessage()` é gravado em `execucoes_agente.erro` e devolvido por `/api/auditoria`. Mensagens de exceção podem conter detalhe interno. | `AuditoriaService.java:95`, `AuditoriaController.java` |
| S09 | MÉDIA | Sem handler global: o formato de erro é o padrão do Spring Boot, que pode incluir detalhes dependendo da versão e da configuração. NÃO VERIFICADO em execução. | ausência de `@ControllerAdvice` |
| S10 | MÉDIA | Credenciais de desenvolvimento em texto claro em arquivos versionáveis; Mongo sem autenticação com porta publicada. | `application.yaml:11-14,24-26`, `docker-compose.yml` |
| S11 | BAIXA | `show-sql: true` imprime SQL no log. Os parâmetros não aparecem por padrão. | `application.yaml:19` |
| S12 | BAIXA | Springdoc exposto. Em produção revela o contrato. Sem configuração para desligar. | `pom.xml:86-90` |

### Entrada e ingestão (OWASP API4)

| ID | Sev. | Achado | Evidência |
|---|---|---|---|
| S13 | MÉDIA | Sem limite de tamanho em `texto` (ingestão), `pergunta` ou `comando`. Ingestão grande gera muitas chamadas de embedding (custo) e uma pergunta enorme vai ao modelo. | DTOs; `ConhecimentoService.java:27-43` |
| S14 | MÉDIA | Sem limite de taxa por usuário, de orçamento, de iterações de tool nem de `maxTokens`. Custo sem teto. | ausência de configuração |
| S15 | MÉDIA | `/api/agente/acoes` usa `Map<String,String>` sem validação de tamanho ou formato. | `AcaoAgenteController.java:31-40` |

### Segurança de LLM

| ID | Sev. | Achado | Evidência |
|---|---|---|---|
| S16 | ALTA | A defesa contra injeção indireta é só uma linha do prompt. Contexto recuperado (não confiável) e instruções estão na mesma mensagem de usuário, sem delimitadores estruturados. | `AtendimentoService.java:47-71` |
| S17 | ALTA | A tool de e-mail aceita o destinatário escolhido pelo modelo, sem lista permitida nem validação de formato. Um comando ou texto malicioso pode induzir uma solicitação para um destinatário arbitrário, que só é barrada pela aprovação humana (que por sua vez não tem identidade, ver S03). | `EmailTools.java:25-33` |
| S18 | MÉDIA | A saída do modelo não é validada além da conversão JSON (`fontes` e `informacaoEncontrada` são aceitos como vierem). | `AtendimentoResultado.java`, `AtendimentoService.java:111-113` |
| S19 | MÉDIA | O prompt completo (regras + contexto + pergunta) é gravado na memória e reenviado em turnos seguintes. Conteúdo recuperado persiste no Mongo e pode reaparecer em turnos de outra pergunta. Inferência do código; comportamento NÃO VERIFICADO. | `AtendimentoService.java:87-88`, `AiConfig.java` |
| S20 | MÉDIA | Sem filtro por dono na busca vetorial: todo conteúdo ingerido é visível a todos os atendimentos. | `ConhecimentoService.java:46-48` |
| S21 | BAIXA | Modelo referenciado por alias sem versão fixa. | `application.yaml:43` |
| S22 | BAIXA | Prompt de sistema/regra só em texto; não há teste de injeção nem eval adversarial. | ver `11-testes.md` |

### Integridade do HITL

| ID | Sev. | Achado | Evidência |
|---|---|---|---|
| S23 | MÉDIA | O e-mail é enviado antes de persistir `ENVIADO`. Falha entre os dois passos deixa o registro `PENDENTE` e permite reenvio. | `EmailService.java:53-59` |
| S24 | BAIXA | `synchronized` só protege uma instância. Com mais de uma réplica, a proteção passa a depender do `@Version`, e o envio SMTP ainda pode duplicar. | `EmailService.java:35,62` |
| S25 | BAIXA | O assunto e o corpo do e-mail são gerados pelo modelo e enviados como texto livre. | `EmailTools.java`, `EmailService.java:46-51` |

## 3. LGPD

| ID | Achado | Evidência / estado |
|---|---|---|
| L01 | Perguntas e comandos podem conter dado pessoal e dado de processo judicial. Seguem para a OpenAI sem mascaramento ou pseudonimização. | `AtendimentoService.java`, `AcaoAgenteController.java` |
| L02 | Contrato com o provedor (papel de operador, não uso para treino, retenção, transferência internacional – art. 33) não consta no repositório. | NÃO VERIFICADO. Requer confirmação humana |
| L03 | Memória no Mongo, auditoria e solicitações no Postgres não têm prazo de retenção nem rotina de exclusão. | ausência no código |
| L04 | `execucoes_agente.erro` e o conteúdo dos e-mails guardam texto livre que pode ter dado pessoal. | `AuditoriaService.java:95`, `SolicitacaoEmail.java` |
| L05 | Não há como apagar os dados de um titular: nem chunks, nem memória, nem auditoria. | ausência de endpoints e rotinas |
| L06 | Decisão automatizada: o sistema responde com `encaminharEspecialista` e `informacaoEncontrada`, mas não há registro dos trechos recuperados nem canal de revisão. A auditoria guarda só metadados de consumo. | `AuditoriaService.java` |
| L07 | Finalidade e base legal do tratamento não estão documentadas. | NÃO ENCONTRADO |

## 4. Mapa resumido OWASP LLM

| Tema | Estado |
|---|---|
| Injeção de prompt (direta e indireta) | exposto: só defesa por prompt |
| Divulgação de informação sensível | exposto: sem mascaramento, sem filtro por dono |
| Cadeia de suprimentos | versões do BOM fixadas (`pom.xml`); modelo por alias; sem verificação de integridade |
| Envenenamento de dados/embeddings | exposto: ingestão aberta e sem origem rastreável por documento |
| Tratamento inadequado de saída | parcial: conversão JSON sem validação semântica; saída não vai a SQL/shell/HTML |
| Agência excessiva | controlado em parte: única tool não envia; destinatário livre |
| Vazamento de system prompt | prompts não têm segredos, mas nada impede a extração |
| Fraqueza em vetores/embeddings | exposto: sem controle de acesso por documento |
| Desinformação | mitigada só por prompt; sem eval |
| Consumo ilimitado | exposto (S13, S14) |
