# Pendente — SPEC-RD-007 (Estratégias e histórico)

Peça para a IA, na próxima sessão: **"Implemente o PENDENTE-RD-007.md"**.

Este documento já identifica os gaps concretos entre o código atual
(`ui/estrategias/`) e `docs/redesign/specs/007-estrategias-e-historico.md` —
não é preciso reaudita tudo do zero, só corrigir o que está listado abaixo e
depois conferir o resto do spec.

Branch a criar: `mobile/rd-007-estrategias` a partir de `main`.

## Gaps já identificados (corrigir)

1. **Selos sem quebra de linha** — podem vazar em 360dp/fonte 150%:
   - `EstrategiasScreen.kt`, `ResumoDeSaude()` (linha ~189): `Row` com os
     selos de contagem por nível de saúde ("2 em dia · 1 em risco").
   - `EstrategiasScreen.kt`, `CartaoEstrategia()` (linha ~215): `Row` com
     selo de categoria + selo de campanha (campanha pode ter nome longo).
   - `HistoricoEstrategiasScreen.kt` (linha ~72): `Row` com selo de ação +
     selo de categoria no cartão do histórico.
   - **NÃO usar `FlowRow`** (`androidx.compose.foundation.layout.FlowRow`)
     para resolver isso. Foi tentado em RD-005: compila com
     `@OptIn(ExperimentalLayoutApi::class)`, mas falha em runtime nos testes
     (`CartaoIdeiaTest`) com `java.lang.NoSuchMethodError` em toda
     renderização que passa por ele — incompatibilidade de resolução de
     dependência entre o classpath de compilação e o de teste (Robolectric)
     nesta versão do BOM do Compose (`composeBom = "2024.09.00"`,
     `mobile/gradle/libs.versions.toml`). Foi revertido para `Row` simples
     no PR #14. Se quiser investigar e resolver de vez (ex.: forçar uma
     versão explícita de `androidx.compose.foundation` nas dependências do
     `mobile/app/build.gradle.kts`, já que hoje ela só vem transitiva via
     `material3`), é um projeto separado — não bloquear RD-007 nisso.
   - Alternativa mais simples e seguramente compatível: aceitar que selos
     continuam em `Row` (overflow horizontal) mas garantir que o texto dos
     selos não seja absurdamente longo, ou envolver o `Row` num
     `horizontalScroll(rememberScrollState())` como paliativo — qualquer
     uma dessas é melhor que reintroduzir `FlowRow` sem resolver a causa
     raiz do `NoSuchMethodError`.

2. **Filtros categoria/campanha não empilham em tela estreita** —
   `HistoricoEstrategiasScreen.kt` linha ~57: os dois `Seletor` estão num
   `Row` fixo com `weight(1f)` cada. O spec pede explicitamente: "Filtros
   categoria/campanha, hoje lado a lado, tornam-se empilhados em largura
   pequena/fonte ampliada." Hoje eles **nunca** empilham. Usar
   `BoxWithConstraints` (ou um breakpoint simples por `maxWidth`) para virar
   `Column` abaixo de uma largura mínima, ou usar `FlowRow` também aqui
   (mais simples, mas spec pede empilhado — "lado a lado" virando "um embaixo
   do outro", não lado a lado quebrando feio no meio).

3. **Filtro de histórico é só client-side, não vai para a API** —
   `HistoricoEstrategiasScreen.kt` (linhas 39-49): `categoria`/`campanha` são
   `state` local da Composable e o filtro é um `.filter { }` em memória sobre
   a lista completa. `HistoricoEstrategiasViewModel.carregar()`
   (`EstrategiasViewModels.kt`, método `carregar`) chama
   `estrategias.historico()` **sem argumentos**, embora a interface
   (`domain/repository/Repositorios.kt:64`) aceite
   `historico(categoria: String? = null, campanha: String? = null)`.
   O critério de aceite do spec exige: "Histórico envia categoria/campanha ao
   repositório; retry usa os mesmos valores." Hoje isso não acontece.
   - Fix: mover `categoria`/`campanha` para o `HistoricoEstrategiasViewModel`
     (como `StateFlow`, igual ao padrão de `IdeiasViewModel.filtro`), um
     método `filtrarPor(categoria, campanha)` que guarda o valor e chama
     `carregar()`, e `carregar()` passando esses valores para
     `estrategias.historico(categoria, campanha)`. A lista de opções de
     categoria/campanha para os `Seletor` pode continuar vindo da própria
     lista carregada (ou de uma chamada sem filtro feita uma vez) — decidir
     o que for mais simples sem quebrar o contrato.
   - Isso também resolve "retry usa os mesmos valores": ao manter os filtros
     no VM, `aoTentarNovamente = vm::carregar` já reusa o que está no
     `StateFlow`.

4. **Diálogo de exclusão não confirma o título do item** —
   `EstrategiaDetalheScreen.kt` linha ~96: `DialogoConfirmacao(titulo =
   "Excluir estratégia?", texto = "A estratégia será removida. ...")` — texto
   genérico, não menciona o nome da estratégia. O spec pede: "Exclusão
   confirma o título do item." Mesmo padrão já usado em
   `ui/ideias/IdeiasScreen.kt` (`"\"${ideia.titulo}\" será removida."`) e em
   `ui/projetos/ProjetoDetalheScreen.kt`. Ajustar o texto para incluir
   `estrategia.titulo` (ou o nome equivalente disponível no estado da tela).

## Conferir (provavelmente já ok, mas não verificado a fundo nesta sessão)

- Formulário (`EstrategiaFormScreen.kt`): se erro ao salvar preserva os
  campos digitados (parece que sim, pelo padrão `BaseViewModel`/`aviso`, mas
  não testado explicitamente).
- `null` em campanha/autor no histórico: **já está ok** —
  `HistoricoEstrategiasScreen.kt` usa `registro.campanha ?: "—"` e
  `registro.autorNome ?: "—"`, sem literal `"null"`. Não precisa mexer.
- Gate de permissão de escrita (criar/editar/excluir só para LIDERANCA): já
  confirmado correto em `EstrategiasScreen.kt` (FAB só `lideranca`) e
  `EstrategiaDetalheScreen.kt` (editar/excluir só `Perfil.LIDERANCA`). Não
  precisa mexer.
- VIGENTE/ENCERRADA sem duplicação de rótulo por cor: dar uma olhada rápida,
  não foi verificado.

## Testes a criar (pedidos pelo spec, nenhum existe hoje)

- `EstrategiasViewModelTest.kt` (só existe `EstrategiasViewModelsTest.kt`
  hoje — confirmar se já cobre `EstrategiasViewModel` ou se falta).
- `HistoricoEstrategiasViewModelTest.kt` — cobrir o novo filtro por
  categoria/campanha indo para o repositório (item 3 acima), com
  `FakeEstrategiaRepository` configurável.
- `EstrategiasConteudoTest.kt` — se fizer sentido extrair um `Conteudo` puro
  como em RD-004/RD-005; senão, documentar o corte de escopo (mesmo padrão
  usado em RD-003/RD-006/RD-005).

## Ao terminar

- Rodar `./gradlew testDebugUnitTest lintDebug assembleDebug` localmente se
  possível (neste sandbox não dá — plugin do Android não resolve pela rede
  configurada; só confiar no CI do GitHub mesmo).
- Commit em português explicando o porquê, push em `mobile/rd-007-estrategias`,
  abrir PR com rodapé `🤖 Generated with Claude Code` + link da sessão.
- Atualizar `docs/redesign/HANDOFF.md` (tabela de status) e apagar este
  arquivo (`PENDENTE-RD-007.md`) quando o PR for aberto — ele já terá
  cumprido o papel.
