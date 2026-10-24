# SPEC-012: Extrair helper comum para filtros dinâmicos de consulta

## Status
DONE

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
`util/Consultas.java` ganhou 3 helpers: `seIgual` (enum/objeto), `seInformado` (String) e
`seInformadoIgnorandoCaixa`. Substituídos, um serviço por vez com teste de integração do
respectivo controller entre cada troca (sem teste novo — a própria IT existente é a prova,
como a spec previu): `IdeiaService.listar`, `ProjetoService.listar`, `EstrategiaService`
(`listar` e `historico`), `DashboardService.projetosDoEscopo`. `DashboardService.resumoIdeias`
ficou de fora de propósito: é um `? :` (filtro OU busca limitada), formato diferente do
"adicionar critério opcional", forçar no helper reduziria clareza em vez de aumentar.

Efeito colateral de limpeza: `ProjetoService` não usava mais `Criteria` diretamente depois da
troca — import removido.

Interrupção no meio do caminho (não é bug meu): o Docker deste sandbox caiu entre uma
mensagem e outra da conversa (religou com uma porta de proxy diferente, sinal de reinício do
ambiente) e o primeiro teste de regressão falhou por "Could not find a valid Docker
environment" - não por causa do refactor. Resolvido religando o daemon antes de seguir.

Suíte completa: 272/272 verdes.
