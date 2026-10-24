# SPEC-005: Índices em campos usados em filtro/ordenação

## Status
PENDING

## Prioridade
IMPORTANTE

## Execução
Autônoma (código local, backend)

## Problema
`Ideia.status`, `Projeto.status`/`etapa`, `Estrategia.status`, e `criadoEm` (usado para
ordenação em várias listagens) não têm `@Indexed`, apesar de serem filtrados/ordenados com
frequência (`IdeiaService.listar`, `ProjetoService.listar`, `EstrategiaService`,
`DashboardService`).

## Critério de aceite
- [ ] Os campos identificados têm `@Indexed` (ou `@CompoundIndex` quando filtro+ordenação
      acontecem juntos, ex. `status` + `criadoEm`).
- [ ] Teste confirma que o índice existe de fato na coleção (via `MongoTemplate.indexOps`),
      não só que a anotação está no código.

## Plano TDD
1. **RED**: teste de integração que sobe o contexto, consulta
   `mongoTemplate.indexOps("ideias").getIndexInfo()` (e equivalente para `projetos`,
   `estrategias`) e espera encontrar um índice cobrindo `status`. Confirmar que FALHA hoje.
2. **GREEN**: adicionar as anotações `@Indexed`/`@CompoundIndex` nos campos dos modelos.
   `spring.data.mongodb.auto-index-creation: true` já está ativo (`application.yml:8`), então
   os índices são criados no boot automaticamente.
3. **REGRESSÃO**: suíte completa verde.

## Resultado
(preencher ao final do ciclo TDD, com números reais de execução)
