# SPEC-RD-005 — Ideias, filtros e decisão

**Prioridade:** P1 • **Dependências:** RD-002, RD-004 • **Requisitos:** RF-05, RNF-03/04/06.

## Objetivo

Tornar a lista de ideias escaneável e seus estados/ações claros para operador e gestor, incluindo avaliação IA sem apresentá-la como decisão humana.

## Arquivos

Modificar `ui/ideias/{IdeiasScreen,IdeiaFormScreen,IdeiasViewModel,IdeiaFormViewModel}.kt` conforme necessário. Extrair `CartaoIdeia.kt` e `DialogoDecisao.kt` dentro de `ui/ideias/` ao separar responsabilidades do arquivo atual. Adaptar `IdeiasViewModelTest.kt` existente; criar `IdeiasConteudoTest.kt` e `IdeiaFormConteudoTest.kt` no mesmo pacote de teste.

Manter contratos `DadosIdeia`, `AvaliacaoIdeia`, `StatusIdeia` e callbacks de decidir/editar/excluir/pontuar.

## Como implementar

- [ ] Cartão: título, descrição, status/prioridade, categoria/estratégia/autor, parecer e ações nessa hierarquia. Reduzir cartões aninhados de avaliação usando superfície secundária, sem esconder justificativa/riscos.
- [ ] Converter linhas de selos e ações em layout com quebra quando necessário. “Prioridade alta”, fonte de IA e botões não podem vazar da largura disponível.
- [ ] Operador alterna próprias/aprovadas; somente as próprias editáveis exibem Editar/Excluir. Gestor filtra status, decide e solicita avaliação. Não introduzir ações para liderança por acessar uma rota manualmente.
- [ ] Preservar chaves por `ideia.id`, ordenação por score e reposicionamento no ranking. Item sem nota continua sem nota, não ganha zero inventado.
- [ ] Aprovar/rejeitar abre o diálogo existente com prioridade e parecer; confirmar chama `vm.decidir`, cancelar não chama rede. Usar par `error/onError` em rejeição para evitar texto ilegível sobre o novo rosa.
- [ ] Formulário preserva campos/associação à estratégia e usa erros dos campos quando aplicável. Dados digitados sobrevivem ao erro de envio; salvar não ocorre por recomposição.
- [ ] Estado vazio distingue ausência de ideias de filtro sem resultado; CTA para criar só aparece ao operador autorizado.

## Testes e aceite

- [ ] Testes existentes de VM continuam passando para filtros, avaliação, ranking e atualização da lista.
- [ ] UI com ideia EM_ANALISE própria versus aprovada de outro autor mostra ações corretas. Gestor não recebe botão de criação de ideia.
- [ ] Decisão cancelada não altera repositório; confirmação envia id/status/prioridade/parecer esperados uma vez por ação habilitada.
- [ ] Lista extensa continua lazy; título, justificativa e selos longos não escondem ações em 360 dp/fonte 150%.
- [ ] Avaliação com origem HEURISTICA mostra “Regras”; sugestões não alteram o status da ideia automaticamente.
- [ ] Validar lista, vazio, erro, formulário e diálogo com fixtures; integração real em ambiente descartável ao fechar o fluxo.

**Fora do escopo:** nova busca, comentários, votação, feed social ou nova política de aprovação.
