# 04 — Contratos da API (AS-IS)

Base: `http://localhost:8081` (`application.yaml:1-2`). Nenhum endpoint exige autenticação: não há Spring Security no `pom.xml` e nenhuma verificação de identidade no código.

Springdoc (`springdoc-openapi-starter-webmvc-ui` 2.10.0) está no `pom.xml`, então a UI e o JSON OpenAPI devem existir nos caminhos padrão da biblioteca. **NÃO VERIFICADO** (não foi executado, e não há configuração de springdoc no YAML).

Os códigos HTTP de erro abaixo são o que o código **sugere** com o comportamento padrão do Spring Boot (não há `@ControllerAdvice`). Não foram observados em execução.

## Resumo

| # | Método | Caminho | Controller → método | Entrada | Validação |
|---|---|---|---|---|---|
| 1 | POST | `/api/atendimentos` | `AtendimentoController.executar` | JSON `PerguntaRequest` | `@Valid` + `@NotBlank` |
| 2 | POST | `/api/conhecimentos` | `ConhecimentoController.cadastrar` | JSON `ConhecimentoRequest` | `@Valid` + `@NotBlank` |
| 3 | GET | `/api/conhecimentos/pesquisar` | `ConhecimentoController.pesquisar` | query `pergunta` | só presença do parâmetro |
| 4 | POST | `/api/agente/acoes` | `AcaoAgenteController.executar` | JSON `Map<String,String>` | manual (`comando`, `conversaId`) |
| 5 | GET | `/api/aprovacoes/pendentes` | `AprovacaoController.pendentes` | — | — |
| 6 | POST | `/api/aprovacoes/{id}/aprovar` | `AprovacaoController.aprovar` | path `id`, query `responsavel` | só presença do parâmetro |
| 7 | POST | `/api/aprovacoes/{id}/rejeitar` | `AprovacaoController.rejeitar` | path `id`, query `responsavel` | só presença do parâmetro |
| 8 | GET | `/api/auditoria/usuario/{usuarioId}` | `AuditoriaController.consultar` | path `usuarioId` | — |

Também há `/actuator/health`, `/actuator/info` e `/actuator/metrics` (`application.yaml:56-60`).

## 1. POST `/api/atendimentos`

Request (`PerguntaRequest`, `dtos/PerguntaRequest.java`):

```json
{ "usuarioId": "string", "conversaId": "string", "pergunta": "string" }
```

Os três campos são `@NotBlank`. Não há limite de tamanho.

Response 200 (`AtendimentoResultado`, `dtos/AtendimentoResultado.java`):

```json
{
  "assunto": "string",
  "resposta": "string",
  "fundamentacao": "string",
  "fontes": ["string"],
  "informacaoEncontrada": true,
  "encaminharEspecialista": false
}
```

Os valores vêm do modelo, convertidos por `BeanOutputConverter`. Nenhuma validação adicional é feita sobre a saída.

Erros observáveis no código:
- Campo vazio ou ausente: falha do `@Valid` (esperado 400, NÃO VERIFICADO o formato do corpo).
- Resposta nula do modelo: `IllegalStateException("O modelo não retornou uma resposta.")` (`AtendimentoService.java:99-102`), sem handler, portanto esperado 500.
- Falha do provedor ou da conversão: a exceção é registrada em auditoria e relançada (`AtendimentoService.java:115-124`), esperado 500.

## 2. POST `/api/conhecimentos`

Request (`ConhecimentoRequest`): `{ "texto": "string", "fonte": "string" }`, ambos `@NotBlank`, sem limite de tamanho.

Response 200: texto puro `Conhecimento armazenado.` (`ConhecimentoController.java:36`).

Efeito: divide `texto` em blocos de 1000 caracteres e grava cada um no `VectorStore` com metadados `fonte` e `dominio="trabalhista"` (`ConhecimentoService.java:19-44`). Não retorna identificadores dos blocos criados.

## 3. GET `/api/conhecimentos/pesquisar?pergunta=...`

Response 200: `List<String>` com o texto dos trechos (`Document::getText`). Os metadados, inclusive a fonte, **não** são devolvidos (`ConhecimentoController.java:42-45`). A quantidade de trechos depende do padrão de `similaritySearch(String)`, **NÃO VERIFICADO**.

## 4. POST `/api/agente/acoes`

Request: JSON livre lido como `Map<String,String>`. Campos usados: `comando`, `conversaId`. Qualquer outro campo é ignorado. Valores não string podem falhar na desserialização, NÃO VERIFICADO.

Response 200: `{ "resultado": "<texto do modelo>" }`.

Erros: `comando` ou `conversaId` nulo/em branco gera `IllegalArgumentException` (`AcaoAgenteController.java:36-40`), sem handler, esperado 500 (não 400).

Efeito colateral: o modelo pode chamar `solicitarEnvioEmail`, que cria um registro `PENDENTE` em `solicitacoes_email`. Este endpoint não recebe `usuarioId`.

## 5. GET `/api/aprovacoes/pendentes`

Response 200: `List<SolicitacaoEmail>` (entidade JPA serializada diretamente):

```json
[{
  "id": 1, "destinatario": "...", "assunto": "...", "mensagem": "...",
  "status": "PENDENTE", "criadoEm": "...", "decididoEm": null,
  "aprovadoPor": null, "justificativa": null, "versao": 0
}]
```

Todos os campos da entidade são expostos, inclusive `versao`.

## 6. POST `/api/aprovacoes/{id}/aprovar?responsavel=...`

Response 200: `SolicitacaoEmail` atualizada, com `status = "ENVIADO"`, `aprovadoPor` e `decididoEm`.

Efeito: envia o e-mail por `JavaMailSender` (assunto, texto e destinatário vêm do registro) **antes** de gravar o novo status (`EmailService.java:46-59`).

Erros observáveis no código:
- `id` inexistente: `NoSuchElementException` de `orElseThrow()` (`EmailService.java:38-39`), esperado 500.
- Status diferente de `PENDENTE`: `IllegalStateException("Solicitação não está pendente.")`, esperado 500.
- Falha SMTP: exceção de `mailSender.send`, esperado 500, e o status permanece `PENDENTE`.
- Conflito de versão (`@Version`): exceção de lock otimista no `save`, NÃO VERIFICADO.

`responsavel` é texto livre informado pelo chamador e gravado em `aprovadoPor`. Não é verificado.

## 7. POST `/api/aprovacoes/{id}/rejeitar?responsavel=...`

Mesmo contrato e mesmos erros de (6), com `status = "REJEITADO"` e sem envio de e-mail. O campo `justificativa` existe na entidade, mas **nenhum endpoint o recebe** e o código nunca o preenche.

## 8. GET `/api/auditoria/usuario/{usuarioId}`

Response 200: `List<ExecucaoAgente>` (entidade serializada diretamente):

```json
[{
  "id": "uuid", "usuarioId": "...", "conversaId": "...", "modelo": "...",
  "tokensEntrada": 0, "tokensSaida": 0, "custoEstimadoUsd": 0.0,
  "duracaoMs": 0, "status": "SUCESSO|ERRO", "erro": null, "criadoEm": "..."
}]
```

Qualquer chamador pode consultar o `usuarioId` que quiser. O campo `erro` guarda `exception.getMessage()` bruto (`AuditoriaService.java:95`) e é devolvido na resposta.

## Inconsistências de contrato

- Três estilos de entrada: record validado (1, 2), `Map` com validação manual (4) e `@RequestParam` (6, 7).
- Erros de entrada em (4) viram exceção genérica, e não resposta de validação.
- Não há formato único de erro (ProblemDetail ou outro) nem handler global.
- Não há paginação em (5) e (8).
- Entidades JPA são o contrato de resposta de (5), (6), (7) e (8).
