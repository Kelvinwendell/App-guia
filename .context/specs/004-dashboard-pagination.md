# SPEC-004: Dashboard não deve carregar coleções inteiras na memória

## Status
DONE (com abordagem de limite documentado — ver Resultado)

## Prioridade
CRÍTICO

## Execução
Autônoma (código local, backend)

## Problema
`DashboardService.porEstrategia()` e `resumoIdeias()` chamam `findAll()` sem paginação nem
filtro, carregando a coleção inteira de `projetos`/`ideias`/`estrategias` na memória a cada
requisição. Funciona com dados de demonstração; não escala e é exatamente a tela que a
Liderança usa.

## Critério de aceite
- [ ] As agregações do dashboard usam agregação no banco (pipeline do MongoDB via
      `MongoTemplate`) em vez de `findAll()` + processamento em memória, **ou**, se o volume
      de dados do domínio realmente for pequeno por natureza (poucas estratégias/projetos por
      empresa), documentar essa decisão explicitamente no código com uma constante de limite
      de seguranca (ex.: buscar no máximo N documentos e logar um warning se o limite for
      atingido) — a decisão entre as duas abordagens é tomada durante a implementação, não
      antes.
- [ ] Resultado numérico do dashboard não muda (mesmos números de antes, para os mesmos
      dados) — isso é validado por teste de regressão, não só "não quebrou".

## Plano TDD
1. **RED**: escrever (ou reaproveitar) um teste de integração que popula N documentos
   (N grande o suficiente para expor o problema, ex. 500) e verifica que `DashboardService`
   continua retornando o resumo correto **e** que a chamada não faz um `find()` sem limite/
   projeção (pode ser verificado via `@Indexed`+`explain()` ou simplesmente por tempo de
   execução/contagem de documentos trazidos, dependendo da abordagem escolhida no passo GREEN).
2. **GREEN**: reescrever a consulta para agregação no banco ou aplicar o limite documentado.
3. **REGRESSÃO**: suíte completa verde, números do dashboard idênticos aos de antes da mudança
   (comparar contra o teste `DashboardControllerIT` existente).

## Resultado
**Decisão tomada durante a implementação** (a spec deixou as duas em aberto): optei pelo
limite documentado em vez de reescrever como agregação no banco. Motivo: `DashboardService`
já tem lógica financeira não-trivial em Java (`ProjetoMetricas`: ROI, lucro, produtividade)
compartilhada com `ProjetoMapper`; reescrever isso como pipeline de agregação do MongoDB sob
pressão de tempo arriscava duplicar/divergir essa lógica sem ganho real na escala atual
(projeto estudantil). A auditoria original também listou essa como alternativa válida.

Implementado: `app.dashboard.limite-registros-agregacao` (novo campo em `AppProperties`,
default 5000 em produção). Método privado `todosComLimiteDeSeguranca` substitui os 3 pontos
de `findAll()` irrestrito (`projetos`, `ideias`, `estrategias` dentro de `porEstrategia()` e
`resumoIdeias()`), usando `MongoTemplate.find(new Query().limit(...), ...)`. Quando o limite é
alcançado, loga um `WARN` explícito avisando que os totais não refletem 100% dos dados —
verificado manualmente que a mensagem aparece de verdade no log durante o teste (não só
assumido). Como efeito colateral de limpeza: os campos `ProjetoRepository`/`IdeiaRepository`/
`EstrategiaRepository` do serviço ficaram sem uso nenhum depois da troca (tudo passou a usar
`MongoTemplate` direto) e foram removidos, junto com os imports.

RED confirmado em `DashboardLimiteDeSegurancaIT` (limite de teste = 5, cria 8 estratégias,
espera resposta com ≤ 5 linhas) antes da implementação. GREEN depois, com o log de aviso
aparecendo no console do teste.

Suíte completa: 272/272 verdes — inclui `DashboardControllerIT` (números do dashboard com o
dataset de seed, bem abaixo do limite de produção) continuando com os mesmos valores de
antes, confirmando que nada mudou para o caso normal.
