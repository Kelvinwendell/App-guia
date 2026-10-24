# Pendente — SPEC-RD-009 (Assistente e insights)

Peça para a IA, na próxima sessão: **"Implemente o PENDENTE-RD-009.md"**.

Leia `docs/redesign/specs/009-assistente-e-insights.md` por completo antes
de começar — este documento não repete o spec inteiro, só o que já foi
feito e o que falta.

## Já corrigido (PR #16 — `mobile/rd-009-chat-contraste`, não mesclado ainda)

O único bug concreto já identificado nesta frente foi corrigido: em
`ui/ia/AssistenteIaScreen.kt`, `Balao()` desenhava o texto da mensagem do
usuário sobre fundo `AguiaCores.Primaria` (azul claro) sem `color` explícito
— herdava a cor de texto padrão do tema dark (clara), ilegível sobre fundo
claro. Fix: `color = MaterialTheme.colorScheme.onPrimary` para o balão do
usuário, `onSurface` para o da IA. Sem teste de UI dedicado (não existe
pasta de teste para `ui/ia` ainda).

**Antes de continuar**: confirme se o PR #16 já foi mesclado. Se sim, parta
de `main`. Se não, pode nascer a partir dele (branch
`mobile/rd-009-chat-contraste`) ou de `main` + reaplicar o fix.

## O que falta (auditoria completa do spec, não feita ainda)

Esta sessão não chegou a auditar o assistente/insights por completo contra
o spec 009. Passos sugeridos:

1. Ler `docs/redesign/specs/009-assistente-e-insights.md` inteiro.
2. Arquivos relevantes: `ui/ia/AssistenteIaScreen.kt` (chat, já tem o fix de
   contraste), `ui/ia/AssistenteViewModel.kt`, e qualquer tela de "insights"
   separada se existir (confirmar — pode estar dentro do dashboard ou de
   `ui/estrategias/EstrategiaDetalheScreen.kt`, que já usa
   `vm.lerComIa()`/`LeituraSaude` para leitura de saúde por IA).
3. Comparar cada item do "Como implementar" do spec com o código atual, no
   mesmo espírito das auditorias de RD-005/RD-007:
   - Mensagens de IA deixam claro que são sugestão, não decisão automática
     (verificar se algum texto implica ação automática sem confirmação
     humana — o padrão do resto do app, estabelecido em RD-005, é a IA
     sugerir e o humano decidir).
   - Estado "IA não configurada no servidor" (`Selo` em
     `ConteudoAssistente`) e estados de carregamento/erro do chat cobertos.
   - `Sugestoes` (perguntas prontas) e `PERGUNTAS_SUGERIDAS` ainda fazem
     sentido pelo spec, ou o spec pede algo diferente.
   - Teclado/IME não esconde o campo de texto do chat; lista rola para a
     mensagem mais recente (já tem `animateScrollToItem`, confirmar que
     cobre o caso de teclado abrindo).

## Testes

Não existe pasta `src/test/.../ui/ia` ainda. Criar conforme o spec pedir —
provavelmente um `AssistenteViewModelTest.kt` com `FakeIaRepository` (seguir
o padrão `naoUsado<T>()` já usado em todo o projeto) e, se fizer sentido,
um teste de UI para o `Balao()` confirmando a cor de texto por autor da
mensagem (`PapelMensagem.USUARIO` vs `IA`).

## Ao terminar

- Commit em português, push em `mobile/rd-009-assistente` (ou continuar em
  `mobile/rd-009-chat-contraste` se ainda não tiver PR mesclado),
  abrir/atualizar PR com rodapé `🤖 Generated with Claude Code` + link da
  sessão.
- Atualizar `docs/redesign/HANDOFF.md` e apagar este arquivo quando o PR
  estiver aberto.
