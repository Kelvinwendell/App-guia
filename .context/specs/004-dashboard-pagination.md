# SPEC-004: Dashboard não deve carregar coleções inteiras na memória

## Status
PENDING

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
(preencher ao final do ciclo TDD, com números reais de execução)
