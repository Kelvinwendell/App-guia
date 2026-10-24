# Pendente — SPEC-RD-010 (Validação final e fechamento)

Peça para a IA, na próxima sessão: **"Implemente o PENDENTE-RD-010.md"**.

Este é o último passo do redesign. Só começar depois que RD-007, RD-008 e
RD-009 (ver `PENDENTE-RD-007.md`, `PENDENTE-RD-008.md`, `PENDENTE-RD-009.md`)
estiverem com PR aberto (não precisa estar mesclado, mas precisa existir).

## Passos

1. Ler `docs/redesign/specs/010-*.md` (confirmar o nome exato do arquivo) por
   completo.
2. Conferir o estado real de cada PR do redesign (`gh`/GitHub MCP:
   `mcp__github__list_pull_requests` ou `pull_request_read`) — CI verde,
   sem conflito de merge, sem comentário de review pendente. Nesta sessão,
   os PRs abertos eram #13 (RD-004), #14 (RD-005), #15 (RD-008 parcial) e
   #16 (RD-009 parcial) — confirmar números atuais, pode já ter mudado.
3. Atualizar a tabela de status em `docs/redesign/README.md` para refletir
   a realidade: o que foi mesclado, o que está em PR, o que ficou de fora
   (cortes de escopo documentados em cada PR — `IdeiaFormConteudoTest.kt`
   não criado em RD-005, por exemplo).
> Atualização (24/10/2026): os passos 2 e 3 foram feitos e houve validação real em emulador para RD-007/008/009
> (`entregas/redesign/RD-010/RELATORIO.md`). O que segue pendente está na seção "Não verificado" do relatório.

4. **Não fingir validação visual.** Este projeto foi trabalhado em sandbox
   sem emulador/device Android real — nenhuma captura de tela, teste manual
   em 360dp/fonte 150%, ou verificação visual de contraste foi feita de
   fato, só inferida lendo código e valores de cor. Os specs pedem
   explicitamente "capturar lista/formulário/etc." e "rodar a regressão
   mobile" — documentar no README, de forma explícita, que isso **não foi
   feito** e precisa ser feito por alguém com acesso a um
   device/emulador antes do release. Não marcar nenhum item de validação
   visual como concluído sem essa verificação real.
5. Depois que o README estiver atualizado e todos os PRs tiverem CI verde:
   apagar os arquivos `docs/redesign/PENDENTE-RD-*.md` e o
   `docs/redesign/HANDOFF.md` (ou arquivá-los em vez de apagar, se preferir
   manter histórico) — eles eram só muletas de continuidade entre sessões,
   e com o README atualizado deixam de ser necessários.

## Decisão que já foi tomada e não deve ser reaberta

- Tema é **dark-only** (ver PRD, "Não objetivos"). Não criar tema claro.
- O trabalho de RD-004 em diante foi construído em cima da reestruturação
  de abas feita pelo PR #9 (outra sessão), não a substituindo.
