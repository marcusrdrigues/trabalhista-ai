# 12 — Dívidas técnicas e inconsistências (AS-IS)

Registro do que foi observado, **sem correção aplicada**. A prioridade é uma sugestão de leitura, não decisão: a ordem real cabe ao Marcus. Referências de segurança (`Sxx`, `Lxx`) estão em `09-seguranca.md`.

## 1. Prioridade alta

| ID | Dívida | Evidência | Relacionado |
|---|---|---|---|
| D01 | Sem autenticação e autorização em todos os endpoints. Identidades (`usuarioId`, `responsavel`) vêm do cliente. | todos os controllers | S01–S03 |
| D02 | Aprovação de envio de e-mail sem identidade verificada, e destinatário livre. Ação com efeito externo. | `AprovacaoController`, `EmailTools` | S03, S17 |
| D03 | Defesa contra injeção indireta só no texto do prompt; contexto e instruções na mesma mensagem. | `AtendimentoService.java:47-71` | S16 |
| D04 | Ingestão aberta, sem limite, sem dono, sem origem por documento, sem remoção. | `ConhecimentoService`, `ConhecimentoController` | S04, S13, S20, L05 |
| D05 | Dados pessoais enviados à OpenAI sem mascaramento; contrato e base legal não documentados; sem retenção nem exclusão. | `AtendimentoService`, `AcaoAgenteController` | L01–L05 |
| D06 | Sem testes além de `contextLoads`; sem eval; sem testes de injeção. | `src/test` | `11-testes.md` |
| D07 | Sem limites de consumo (tokens de saída, taxa por usuário, orçamento, tamanho de entrada). | ausência de configuração | S13, S14 |

## 2. Prioridade média

| ID | Dívida | Evidência |
|---|---|---|
| D08 | Auditoria duplicada: `registrar` seguido de falha em `convert` gera também um registro `ERRO` para o mesmo atendimento. | `AtendimentoService.java:104-124` |
| D09 | A busca vetorial roda fora do `try`; sua falha não é auditada. | `AtendimentoService.java:36-37` |
| D10 | O fluxo de ação do agente não é auditado (sem tokens, custo, erro) e não grava quem pediu. | `AcaoAgenteController.java` |
| D11 | Chaves de memória inconsistentes entre os fluxos (`usuarioId:conversaId` e `conversaId`). | `AtendimentoService.java:89-92`, `AcaoAgenteController.java:54-57` |
| D12 | Envio SMTP antes de persistir `ENVIADO`; sem `@Transactional`; `synchronized` não vale entre instâncias; sem chave de idempotência na tool. | `EmailService.java:35-60`, `EmailTools.java` |
| D13 | Sem handler global de exceções. `orElseThrow()`, `IllegalStateException` e `IllegalArgumentException` não viram 404/409/400 de forma explícita. | ausência de `@ControllerAdvice` |
| D14 | Entidades JPA expostas pela API e com campos `public`. | `AprovacaoController`, `AuditoriaController`, `entities/*` |
| D15 | Esquema por `ddl-auto: update`, sem migrações versionadas. | `application.yaml:18` |
| D16 | Prompts em strings no código, sem versão; o de atendimento mistura regras, contexto e pergunta numa só mensagem de usuário. | `AtendimentoService`, `AcaoAgenteController` |
| D17 | Chunking de 1000 caracteres sem sobreposição, ignorando estrutura (artigos); o próprio código registra que é "para demonstração". | `ConhecimentoService.java:21-32` |
| D18 | `topK`, limiar e filtro não definidos; metadado `dominio` gravado e nunca usado. | `ConhecimentoService.java:34-40,46-48` |
| D19 | `ingestão` faz um `vectorStore.add` por bloco e pode ficar parcial em falha. | `ConhecimentoService.java:27-43` |
| D20 | Prompt completo com contexto vai para a memória e é reenviado nos turnos seguintes (inferido). | `AtendimentoService.java:87-88` |
| D21 | Modelo por alias sem versão; sem timeout, retry, `maxTokens` nem modelo de reserva configurados. | `application.yaml:41-44` |
| D22 | `/api/agente/acoes` usa `Map<String,String>` em vez de DTO validado. | `AcaoAgenteController.java:31` |

## 3. Prioridade baixa

| ID | Dívida | Evidência |
|---|---|---|
| D23 | Campo `justificativa` existe e nunca é escrito; rejeição não pede motivo. | `SolicitacaoEmail.java:36-37` |
| D24 | `status` como `String` livre em vez de enum. | `SolicitacaoEmail.java:28`, `ExecucaoAgente.java:31` |
| D25 | Sem índice declarado em `execucoes_agente.usuario_id` nem paginação em `/auditoria` e `/pendentes`. | `ExecucaoAgente.java`, controllers |
| D26 | `@Value` em campo nos serviços em vez de configuração tipada. | `AuditoriaService.java:18-22`, `EmailService.java:20-21` |
| D27 | `AuditoriaController` e `AcaoAgenteController` acessam repository/modelo direto, sem service. | os dois controllers |
| D28 | Duração mede só a chamada ao modelo, não a requisição. | `AtendimentoService.java:83` |
| D29 | Custo de embeddings não contabilizado. | `AuditoriaService` |
| D30 | `/pesquisar` devolve só o texto, sem fonte. | `ConhecimentoController.java:42-45` |
| D31 | Credenciais de desenvolvimento em arquivos versionáveis; Mongo sem autenticação. | `application.yaml`, `docker-compose.yml` |
| D32 | Sem perfis de ambiente; `show-sql` ligado; springdoc sem restrição. | `application.yaml` |
| D33 | Ausência de logger de aplicação. | todas as classes |
| D34 | Sem `Dockerfile` nem CI. | raiz do repositório |

## 4. Inconsistências entre a intenção e a implementação

| Tema | Esperado pelo contexto do pedido | Encontrado |
|---|---|---|
| Sistemas multiagentes | análise inclui "comunicação MCP e delegação entre agentes" | Não há MCP, A2A, REST entre agentes nem delegação. Há dois fluxos independentes no mesmo `ChatClient`. |
| Tracing | "fluxo de tracing" | Só auditoria por tabela. Não há trace id nem tracing distribuído. |
| RAG | "RAG, embeddings e PgVector" | Implementado em versão mínima; sem filtro, sem remoção, sem avaliação. |
| Constituição Spec Kit | projeto preparado para Spec Kit | `.specify/memory/constitution.md` ainda é o template com placeholders. |
| Repositório | código "já implementado" | O código implementado está sem commit; o único commit é o esqueleto. |
| Descrição da tool | "NUNCA envia o e-mail" | Verdadeiro para a tool. O envio existe em `EmailService.aprovar`, acessível por endpoint aberto. |

## 5. Pontos do código com comentário de intenção

- `ConhecimentoService.java:21-23`: "Quebra simples para demonstração. Em produção, utilizar splitters por tokens e preservar referências a artigos/documentos." Registra que o chunking atual é provisório.
