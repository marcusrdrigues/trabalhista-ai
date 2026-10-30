# 11 — Testes (AS-IS)

## 1. Testes existentes

| Teste | Arquivo | Tipo | O que cobre |
|---|---|---|---|
| `contextLoads` | `src/test/java/br/com/cotiinformatica/trabalhista_ai/TrabalhistaAiApplicationTests.java` | `@SpringBootTest` | Apenas que o contexto Spring sobe |

É o único arquivo de teste do repositório (`src/test` tem só ele). Há também `target/test-classes/...TrabalhistaAiApplicationTests.class`, que é o resultado da compilação.

## 2. Estado de execução

**NÃO VERIFICADO.** O teste não foi executado nesta análise, por dois motivos:

- Ele carrega o contexto completo com o `application.yaml` real, então depende de `OPENAI_API_KEY`, PostgreSQL em `localhost:5434` e MongoDB em `localhost:27018`.
- Ao subir, `ddl-auto: update` e `initialize-schema: true` alterariam os esquemas do banco local. Isso foi evitado para respeitar a restrição de não executar operações com efeito.

Não há resultado de execução anterior no repositório (sem relatório em `target/surefire-reports`).

## 3. Cobertura por funcionalidade

| Funcionalidade | Teste |
|---|---|
| Atendimento com RAG | nenhum |
| Montagem de contexto e de prompt | nenhum |
| Conversão da saída estruturada | nenhum |
| Ingestão e chunking | nenhum |
| Busca vetorial | nenhum |
| Memória conversacional | nenhum |
| Tool `solicitarEnvioEmail` | nenhum |
| Aprovação, rejeição, regras de status | nenhum |
| Envio SMTP | nenhum |
| Auditoria e cálculo de custo | nenhum |
| Validação de DTOs | nenhum |
| Autorização, isolamento por usuário | nenhum (não há o que testar: não há autorização) |
| Tratamento de falha do provedor | nenhum |
| Injeção de prompt | nenhum |

## 4. Ausências em relação ao padrão esperado para projetos de IA

| Item | Estado |
|---|---|
| Teste unitário com o modelo substituído por dublê | NÃO ENCONTRADO |
| Teste de integração com PostgreSQL/pgvector e MongoDB reais (Testcontainers) | NÃO ENCONTRADO (sem dependência) |
| Teste HTTP dos controllers (MockMvc ou similar) | NÃO ENCONTRADO |
| Conjunto de eval versionado, com nota mínima | NÃO ENCONTRADO |
| Casos adversariais (injeção direta e indireta, extração de prompt, dado de outro usuário) | NÃO ENCONTRADO |
| Avaliação separada de recuperação e resposta | NÃO ENCONTRADO |
| Pipeline de CI que rode testes | NÃO ENCONTRADO |

## 5. Observação sobre o teste existente

`contextLoads` roda contra serviços reais e chave real. Se `OPENAI_API_KEY` não estiver definida, a resolução do placeholder `${OPENAI_API_KEY}` deve falhar na subida do contexto, NÃO VERIFICADO. Sem os containers do Compose, o contexto também deve falhar na conexão com os bancos.
