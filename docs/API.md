# Contrato da API

Base: `http://localhost:8080` · Swagger interativo: `/swagger-ui.html` · OpenAPI (JSON): `/v3/api-docs`.
Todas as rotas, exceto as marcadas como *pública*, exigem `Authorization: Bearer <accessToken>`.

## Autenticação

```bash
curl -s -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"anna@gmail.com","senha":"123456"}'
# {"accessToken":"eyJ...","tokenType":"Bearer","expiresIn":28800,"expiraEm":"...","usuario":{"id":"...","perfil":"GESTOR",...}}

curl -s http://localhost:8080/api/ideias -H "Authorization: Bearer $TOKEN"
```

O token é um JWT HS256 (emissor `aguia-inova-api`, validade padrão de 480 min). O **perfil não é lido do token**: o
servidor consulta o usuário a cada requisição, então rebaixar/excluir alguém vale imediatamente.

## Formato de erro

Toda falha volta como JSON (`application/problem+json`), sem detalhes internos:

```json
{"type":"about:blank","title":"Bad Request","status":400,"detail":"Dados inválidos: senha must not be blank",
 "instance":"/api/auth/login","errors":{"senha":"must not be blank"}}
```

| HTTP | Quando |
|---|---|
| 400 | Corpo/parâmetro inválido (`errors` lista os campos) |
| 401 | Sem token, token inválido/expirado, e-mail ou senha incorretos |
| 403 | Perfil sem permissão, ou tentativa de mexer no que é de outro usuário |
| 404 | Recurso inexistente |
| 409 | Conflito: e-mail já cadastrado; ideia já avaliada não pode ser editada/excluída; estratégia com ideias ou projetos vinculados não pode ser excluída |
| 422 | Regra de negócio: estratégia não vigente, prazo anterior ao início, progresso fora de 0–100, projeto só nasce de ideia aprovada, alterar o próprio perfil |
| 503 | Serviço de IA indisponível/não configurado (só no chat, que não tem alternativa por regras) |

## Endpoints

Legenda de perfis: **O** operador · **G** gestor · **L** liderança · **\*** qualquer perfil autenticado.

### Autenticação e usuários
| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | pública | Cadastro (`nome, email, senha≥6, perfil?, cargo?, departamento?, unidade?`). O `perfil` só é respeitado se `APP_ALLOW_ROLE_SELECTION=true`; senão vira Operador |
| POST | `/api/auth/login` | pública | Login → `accessToken` |
| GET | `/api/auth/me` | \* | Usuário logado |
| GET | `/api/usuarios` | L | Lista usuários |
| PATCH | `/api/usuarios/{id}/perfil` | L | Muda o perfil de um usuário |

### Estratégias (liderança escreve; todos leem)
| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| GET | `/api/estrategias?status=&categoria=&campanha=` | \* | Lista (vigentes/encerradas) |
| GET | `/api/estrategias/{id}` | \* | Detalha |
| POST | `/api/estrategias` | L | Cria (`titulo, descricao, categoria, campanha?, status?, metas?, inicio?, fim?`); `metas` até 8 itens de 200 caracteres, `inicio`/`fim` em `yyyy-MM-dd` com `fim` não anterior a `inicio` (422) |
| PUT | `/api/estrategias/{id}` | L | Atualiza (substitui o conteúdo: sem `metas`/`inicio`/`fim` na requisição, eles são limpos) |
| DELETE | `/api/estrategias/{id}` | L | Remove (o histórico permanece) |
| GET | `/api/estrategias/historico?estrategiaId=&categoria=&campanha=&de=&ate=` | \* | **Histórico**: id, data, categoria, campanha, ação (criada/atualizada/removida) e autor |
| GET | `/api/estrategias/{id}/historico` | \* | Histórico de uma estratégia |
| GET | `/api/estrategias/saude` | G, L | **Saúde** de cada estratégia vigente (veja abaixo) |
| GET | `/api/estrategias/{id}/saude` | G, L | Saúde de uma estratégia vigente (422 se estiver encerrada) |

**Saúde da estratégia** é apurada por **regras** (nunca pela IA) a partir do que está vinculado a ela. Resposta:
`nivel` (`EM_DIA`, `EM_RISCO` ou `SEM_TRACAO`), `motivos[]`, `proximaAcao` e os números usados (`ideias`,
`ideiasEmAnalise`, `aprovadasSemProjeto`, `projetos`, `projetosAtrasados`, `investimento`, `retorno`,
`roiPercentual`, `ultimaAtividade`). `EM_RISCO`: período vencido com a estratégia ainda vigente, projeto atrasado, duas ou
mais ideias aprovadas sem projeto ou ROI negativo com projeto concluído. `SEM_TRACAO` (se não há risco): nenhum projeto
em aberto e nenhuma ideia ou mais de 60 dias sem atividade; estratégias com até 14 dias ou que ainda não começaram ganham
carência. Caso contrário, `EM_DIA`. Reavaliar uma ideia pela IA não conta como atividade.

### Ideias
| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| GET | `/api/ideias?status=&prioridade=&categoria=&estrategiaId=` | O, G | O recebe só as suas; G recebe todas (com a nota da IA) |
| GET | `/api/ideias/aprovadas` | \* | Ideias aprovadas da empresa |
| GET | `/api/ideias/{id}` | O, G | Detalha (O só as suas) |
| POST | `/api/ideias` | O | Cria (`titulo, descricao, categoria, estrategiaId?` — estratégia precisa estar vigente) |
| PUT | `/api/ideias/{id}` | O | Edita a própria ideia, enquanto *em análise* |
| DELETE | `/api/ideias/{id}` | O | Exclui a própria ideia, enquanto *em análise* |
| PATCH | `/api/ideias/{id}/avaliacao` | G | Aprova/rejeita e define prioridade (`status, prioridade, parecer?`) |

### Projetos (gestor escreve; liderança lê)
| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| GET | `/api/projetos?status=&etapa=&estrategiaId=` | G, L | Lista: etapa, status, investimento, prazo, retorno |
| GET | `/api/projetos/{id}` | G, L | Detalha, com métricas (lucro, ROI, dias para o prazo, atraso) e resultados |
| POST | `/api/projetos` | G | Cria (`nome, descricao, investimento, prazo, dataInicio?, estrategiaId?, ideiaId?, status?, etapa?`) |
| PUT | `/api/projetos/{id}` | G | Atualiza os dados |
| PATCH | `/api/projetos/{id}/progresso` | G | Atualiza `status`, `etapa` e `progresso` (0–100) |
| POST | `/api/projetos/{id}/resultados` | G | Registra resultado (`descricao, retornoFinanceiro, ganhoProdutividade?, reducaoCustos?, data?`) |
| DELETE | `/api/projetos/{id}/resultados/{resultadoId}` | G | Remove um resultado |
| DELETE | `/api/projetos/{id}` | G | Exclui o projeto |

### Dashboard e relatórios (liderança)
| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/dashboard/resumo?estrategiaId=&projetoId=` | ROI, lucro, investimento e retorno totais, produtividade e custos médios, projetos por status/etapa, ideias, próximos prazos |
| GET | `/api/dashboard/estrategias` | ROI e totais por estratégia (gráfico de barras) |
| GET | `/api/dashboard/projetos?estrategiaId=` | Desempenho por projeto |
| GET | `/api/dashboard/evolucao?estrategiaId=&projetoId=` | Retorno por mês e acumulado (gráfico de colunas/linha) |

### IA
| Método | Rota | Perfil | Descrição |
|---|---|---|---|
| GET | `/api/ia/status` | \* | Se há provedor configurado, qual e qual modelo |
| POST | `/api/ia/ideias/{id}/avaliar` | G | Nota 0–100, alinhamento, prioridade sugerida, pontos fortes e riscos (IA ou regras) |
| POST | `/api/ia/ideias/avaliar-pendentes` | G | Avalia as ideias em análise e devolve o **ranking** |
| POST | `/api/ia/chat` | G | Assistente (`mensagem`, `historico?` até 20 turnos; o servidor não guarda estado) |
| POST | `/api/ia/dashboard/insights` | L | Leitura executiva dos indicadores (IA ou regras) |
| POST | `/api/ia/estrategias/rascunho` | L | Rascunho de estratégia (`titulo, descricao, categoria, campanha?, metas[], inicio?, fim?`) a partir de um `objetivo`. Sem substituto por regras: **503** sem IA. Respeita os limites de `POST /api/estrategias` |
| POST | `/api/ia/estrategias/analise` | L | Critica uma estratégia antes de salvar (`estrategiaId?` + `estrategia`): `nota` 0–100, `pontosFortes`, `melhorias` e `sobreposicoes` com as vigentes (IA ou regras) |
| POST | `/api/ia/estrategias/{id}/saude` | G, L | Explica a saúde da estratégia e recomenda ações (`saude` + `leitura` + `recomendacoes`; IA ou regras). O nível é sempre o das regras |

### Operação
`GET /actuator/health` e `/actuator/info` são públicos (usados pelo healthcheck do Docker).
