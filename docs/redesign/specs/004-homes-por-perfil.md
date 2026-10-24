# SPEC-RD-004 — Homes dos três perfis

**Prioridade:** P1 • **Dependências:** RD-002 • **Requisitos:** RF-04, O2.

## Objetivo

Organizar indicadores compactos e a próxima ação de cada perfil sem perder o acesso às demais funções. As três homes continuam compartilhando componentes e `HomeViewModel`.

## Arquivos e interfaces

Modificar `ui/home/HomeScreens.kt`; criar `ui/home/HomeConteudo.kt` com a assinatura exata definida em ARQUITETURA.md. `HomeScreens.kt` mantém funções públicas e faz a ligação de cada callback com `Rotas`.

`ResumoHome` e `HomeViewModel` permanecem em `HomeViewModel.kt`. Só alterar sua lógica se um caso de teste demonstrar defeito. Criar `ui/home/HomeViewModelTest.kt` e `ui/home/HomeConteudoTest.kt` no source set de teste.

## Conteúdo decidido

| Perfil | Indicadores | Ação primária | Outras ações existentes |
|---|---|---|---|
| Operador | Minhas ideias, Aprovadas | Minhas ideias | Estratégias da empresa |
| Gestor | Ideias pendentes, Projetos ativos, Retorno parcial | Aprovar ideias | Assistente de IA, Criar projeto, Atualizar progresso, Estratégias |
| Liderança | ROI, Custos, Produtividade, Projetos | Dashboard executivo | Gerenciar estratégias, Projetos estratégicos, Histórico de estratégias |

## Como implementar

- [ ] Extrair conteúdo sem alterar chamadas de carregamento e chaves dos VMs por perfil.
- [ ] Indicadores em duas colunas quando couberem. Em fonte grande/largura restrita, permitir coluna única ou quebra natural antes de truncar valores.
- [ ] Retorno parcial ocupa largura integral quando necessário. Usar `Formatadores` e manter valor completo acessível; não substituir o retorno do servidor por número da prévia.
- [ ] Usar `CartaoAcao(destaque = true)` somente na ação primária. A ação continua disponível quando indicador for zero; não há regra de bloqueio por contagem.
- [ ] Garantir rolagem até a última ação e padding inferior sem sobreposição com navegação.
- [ ] Não adicionar saudação dinâmica, avatar remoto ou métricas que exigem endpoint novo.

## Testes e aceite

- [ ] VM com fakes: operador consulta ideias; gestor combina pendentes e projetos, conta ativos e soma retorno; liderança consulta resumo de dashboard. Usar dados conhecidos e conferir o resultado, não repetir a implementação no teste.
- [ ] UI de cada perfil mostra apenas suas ações; cada clique chama o callback correspondente uma vez.
- [ ] Loading não apresenta KPIs falsos; erro inicial tem retry; falha de atualização preserva o resumo anterior.
- [ ] Com zero, moeda extensa e fonte 150%, nenhuma ação essencial desaparece. Validar 360 e 411 dp.
- [ ] Capturar as três homes renderizadas em Android/Robolectric. Simulação Windows fica apenas como referência histórica.

**Fora do escopo:** alterar cálculo de projeto ativo, lucro, ROI ou a API de resumo.
