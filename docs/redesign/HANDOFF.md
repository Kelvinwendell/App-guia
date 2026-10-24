# Handoff — Redesign Águia Inova (continuação em outra sessão)

Este arquivo existe para que, se esta sessão do Claude Code acabar (créditos,
timeout, etc.), outra sessão possa continuar o trabalho sem perder contexto.
Gerado em 2026-10-24.

## Como continuar

1. Leia `docs/redesign/README.md`, `PRD.md`, `ARQUITETURA.md` e os specs em
   `docs/redesign/specs/0XX-*.md` — são a fonte de verdade.
2. Veja a seção "Status por spec" abaixo para saber o que já foi feito, o que
   está em PR aberto e o que falta.
3. Decisão importante já tomada (não reabrir): **tema é dark-only** (PRD,
   "Não objetivos"). Não criar tema claro.
4. O PR #9 (de outra sessão paralela, mesclado direto na `main` pelo usuário)
   reestruturou abas de `ui/home`, `ui/ideias`, `ui/estrategias`,
   `ui/dashboard`, `ui/ia`, e adicionou `historico()`/`saude()`/`saudeDe()` ao
   `EstrategiaRepository` e campos `metas`/`inicio`/`fim` a `Estrategia`. Todo
   trabalho desta lista é construído **em cima** dessa reestruturação (auditar
   contra o spec + corrigir gaps), não substituí-la.
5. Padrão de PR usado em todos os specs: branch `mobile/rd-XXX-nome` a partir
   de `main`, commits em português explicando o motivo (não só o "o quê"),
   PR squash-merge, corpo do PR com rodapé `🤖 Generated with Claude Code` +
   link da sessão. Nunca force-push em `main`.
6. Padrão de teste: `FakeXRepository` implementando a interface com campos
   `Resultado<T>` configuráveis por teste + helper `naoUsado<T>()` para
   métodos não usados no teste. `MainDispatcherRule` para VMs. Para Compose UI
   tests: `createComposeRule()` + `RobolectricTestRunner` + `@Config(sdk =
   [34])`; usar `onNodeWithText`/`onAllNodesWithText` (import necessário) —
   `onNode`/`onAllNodes` são membros da rule, **não** importar.
7. CI mobile: `.github/workflows/mobile-ci.yml` roda
   `testDebugUnitTest`/`lintDebug`/`assembleDebug`. **Não** readicionar
   `android-actions/setup-android@v3` — falha sempre (pacote `tools`
   deprecado); `ubuntu-latest` já tem SDK.
8. Bug crítico já corrigido (não reintroduzir): `AguiaInovaApp.onCreate()` não
   pode construir `AppContainer` (ou qualquer coisa que toque
   `EncryptedSharedPreferences`/`AndroidKeyStore`) **eager** — Robolectric não
   tem Keystore real e quebra toda a suíte de testes. Tem que ser `by lazy`.

## Status por spec (atualizado 24/10/2026, 2ª sessão)

| Spec | Status | Observação |
|---|---|---|
| RD-001 Tema dark-only | ✅ Merged | PR #7 |
| RD-002 Componentes base | ✅ Merged | PR #8; ajuste da barra inferior vai no PR #17 |
| RD-003 Autenticação/Perfil | ✅ Merged | PR #10 |
| RD-004 Homes por perfil | 🟡 PR #13 | CI verde (teste corrigido: texto duplicado e cartões abaixo da dobra) |
| RD-005 Ideias | 🟡 PR #14 | CI verde |
| RD-006 Projetos | ✅ Merged | PR #11 |
| RD-007 Estratégias e histórico | 🟡 PR #17 | CI verde, validado no emulador |
| RD-008 Dashboard | 🟡 PR #15 | completo (não mais parcial), validado no emulador |
| RD-009 Assistente e insights | 🟡 PR #16 | empilhado sobre o #15 (mesclar o #15 antes), validado no emulador sem IA real |
| RD-010 Fechamento | 🟡 Parcial | `entregas/redesign/RD-010/RELATORIO.md` — falta a matriz completa |

Ordem sugerida de merge: #13 → #14 → #17 → #15 → #16.

## Ambiente local que funciona (Windows desta máquina)

- Gradle falha com `Unable to establish loopback connection` se o temp do sistema não for acessível: exporte
  `TEMP=C:\gtmp TMP=C:\gtmp JAVA_TOOL_OPTIONS="-Djdk.net.unixdomain.tmpdir=C:\gtmp -Djava.io.tmpdir=C:\gtmp"` e
  `JAVA_HOME` do JBR do Android Studio.
- API local: MongoDB 7.0 em `C:\ferramentas\mongodb-win32-x86_64-windows-7.0.14` (`mongod --dbpath C:\ferramentas\mongo-data`),
  backend com JDK 25 (`~/.jdks/jdk-25/jdk-25.0.2`) e um `JWT_SECRET` próprio (o valor-padrão público é recusado).
- Emulador `aguia_test`. O APK de **release** instalado aponta para produção; para testar contra a API local é preciso
  desinstalar e instalar o **debug** (assinaturas diferentes).

## RD-005 — estado exato nesta sessão

Branch `mobile/rd-005-ideias` (a partir de `main`, após RD-004 ter sido aberto
como PR).

Arquivos alterados (ainda não commitados ao chegar nesta mensagem, mas serão
commitados nesta mesma rodada da sessão — ver `git log` do branch para
confirmar se o commit já existe):

- `ui/ideias/IdeiasScreen.kt`:
  - Estado vazio agora é um `when` que distingue: filtro do gestor sem
    resultado vs. gestor sem nenhuma ideia vs. aba "aprovadas" vazia vs.
    operador sem nenhuma ideia própria (este último com `MensagemVazia(...,
    acaoTexto = "Cadastrar ideia", aoAcao = { navController.navigate(...) })`).
  - Botão "Rejeitar" usa `MaterialTheme.colorScheme.error`/`onError`
    explicitamente (antes não garantia contraste).
  - Botão "Aprovar" usa `AguiaCores.Fundo` no lugar de `Color.Black` fixo.
  - `AvaliacaoDaIa` passou a usar `Card` com
    `colorScheme.surfaceContainerHigh` (superfície secundária) no lugar do
    mesmo `CartaoAguia` do cartão pai — evita cartão aninhado do mesmo tom.
  - `CartaoIdeia` deixou de ser `private` para poder ser testado isolado.
- `ui/ideias/IdeiaFormScreen.kt`: adicionado `tentouSalvar` (state local) e
  `erro =` em cada `CampoTexto` (título/descrição/categoria) — mostra erro
  inline só depois da primeira tentativa de salvar, sem perder o texto
  digitado e sem disparar `vm.salvar` por recomposição. Atende ao requisito
  do spec "usa erros dos campos quando aplicável".
- `ui/ideias/CartaoIdeiaTest.kt` (novo): 5 testes cobrindo visibilidade de
  ações por perfil/dono e callback de decisão.

**Decisão de escopo tomada**: `IdeiaFormConteudoTest.kt` (pedido pelo spec,
"criar ... no mesmo pacote de teste") **não foi criado** nesta sessão por
restrição de tempo/créditos — mesmo corte de escopo já usado em RD-003
(`AutenticacaoConteudoTest` pulado) e RD-006 (`ProjetosConteudoTest` pulado).
Se for retomado: extrair o conteúdo de `IdeiaFormScreen.kt` (o bloco dentro de
`ConteudoDoEstado { formulario -> ... }`) para uma função pura
`IdeiaFormConteudo(formulario, ocupado, aoSalvar)` como foi feito em
`HomeConteudo.kt` (RD-004) e `PerfilConteudo` (RD-003), e então testar essa
função isolada (sem NavController/ViewModel).

**Não verificado**: `IdeiasViewModelTest.kt` existente — spec pede "adaptar"
mas a lógica de `IdeiasViewModel.kt` não foi alterada nesta sessão, então
provavelmente não precisa de adaptação. Confirmar rodando os testes.

**Próximo passo imediato**: commitar os 3 arquivos acima, dar push no branch
`mobile/rd-005-ideias`, abrir PR (mesmo padrão dos outros).

## RD-007/008/009 — o que fazer

Cada spec restante agora tem um documento próprio, com gaps concretos já
identificados (arquivo/linha) para não ter que reauditar do zero:

- `docs/redesign/PENDENTE-RD-007.md` — Estratégias e histórico (4 gaps
  concretos já mapeados: selos que podem vazar a largura, filtros não empilham, filtro
  de histórico não vai pra API, diálogo de exclusão sem o título do item).
- `docs/redesign/PENDENTE-RD-008.md` — Dashboard (bug da barra zero já
  corrigido no PR #15; auditoria completa do resto do spec ainda falta).
- `docs/redesign/PENDENTE-RD-009.md` — Assistente e insights (contraste do
  chat já corrigido no PR #16; auditoria completa do resto do spec ainda
  falta).
- `docs/redesign/PENDENTE-RD-010.md` — fechamento final, só depois dos três
  acima terem PR aberto.

Para continuar, basta pedir: **"Implemente o PENDENTE-RD-007.md"** (ou 008/
009/010). Cada um já tem o suficiente pra começar direto, sem reler todo o
histórico desta sessão.

## RD-010 — fechamento

Quando os specs 004–009 estiverem todos com PR aberto (ou mesclado):

1. Atualizar a tabela de status em `docs/redesign/README.md` para refletir a
   realidade (specs concluídos, PRs abertos, o que ficou de fora).
2. Confirmar que o CI mobile está verde em todos os PRs abertos.
3. **Não fingir validação visual** — este sandbox não tem emulador/device
   Android real. Documentar explicitamente no README que a validação visual
   (capturas de tela, teste manual em 360dp, fonte 150%, etc.) exigida pelos
   specs **não foi feita** e precisa ser feita por alguém com acesso a um
   device/emulador antes do release final.
4. Remover este `HANDOFF.md` (ou arquivá-lo) quando todo o trabalho estiver
   mesclado e o README já refletir o estado real — ele é só uma muleta de
   continuidade entre sessões.
