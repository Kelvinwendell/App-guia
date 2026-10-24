# SPEC-012: Extrair helper comum para filtros dinâmicos de consulta

## Status
PENDING

## Prioridade
BOM TER

## Execução
Autônoma (código local, backend)

## Problema
O bloco "se filtro != null, addCriteria(...)" se repete, quase idêntico, em
`IdeiaService.listar`, `ProjetoService.listar`, `EstrategiaService`, `DashboardService`.

## Critério de aceite
- [ ] Um helper único (ex. em `util/Consultas.java`) recebe pares (campo, valor opcional) e
      monta os critérios, substituindo a duplicação nos 4 services.
- [ ] Nenhuma mudança de comportamento — mesmos resultados de filtro de antes.

## Plano TDD
1. **RED**: os testes de integração existentes dos 4 controllers (`IdeiaControllerIT`,
   `ProjetoControllerIT`, `EstrategiaControllerIT`, `DashboardControllerIT`) já cobrem os
   filtros — são o contrato. Não precisa teste novo, a regressão é a prova.
2. **GREEN**: extrair o helper e substituir nos 4 pontos, um por vez, rodando a suíte entre
   cada substituição.
3. **REGRESSÃO**: suíte completa verde após cada substituição.

## Resultado
(preencher ao final do ciclo, com números reais de execução)
