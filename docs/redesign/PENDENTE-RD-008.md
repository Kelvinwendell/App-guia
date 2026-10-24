# Pendente — SPEC-RD-008 (Dashboard)

Peça para a IA, na próxima sessão: **"Implemente o PENDENTE-RD-008.md"**.

Leia `docs/redesign/specs/008-dashboard.md` por completo antes de começar —
este documento não repete o spec inteiro, só o que já foi feito e o que
falta decidir/conferir.

## Já corrigido (PR #15 — `mobile/rd-008-dashboard-fix`, não mesclado ainda)

O único bug concreto já identificado nesta frente foi corrigido: em
`ui/components/Graficos.kt`, `GraficoBarras`/`GraficoColunas` desenhavam uma
barra visível mesmo para valor **zero** (o mínimo de fração visível
`MENOR_FRACAO` era aplicado incondicionalmente). Extraída a função
`fracaoDaBarra(valor, maximo)` que retorna `0f` quando `valor == 0.0`. Tem
teste (`GraficosTest.kt`).

**Antes de continuar**: confirme se o PR #15 já foi mesclado. Se sim, parta
de `main`. Se não, pode nascer a partir dele (branch `mobile/rd-008-dashboard-fix`)
ou de `main` + reaplicar o fix — confira primeiro com `git log`/GitHub.

## O que falta (auditoria completa do spec, não feita ainda)

Esta sessão não chegou a ler e auditar o dashboard inteiro contra o spec
008. Passos sugeridos:

1. Ler `docs/redesign/specs/008-dashboard.md` inteiro (seções "Arquivos",
   "Como implementar", "Testes e aceite").
2. Mapear os arquivos atuais de `ui/dashboard/` (prováveis:
   `DashboardScreen.kt`, `DashboardViewModel.kt` — confirmar nomes reais,
   podem ter mudado com o PR #9 de reestruturação de abas).
3. Comparar cada item do "Como implementar" do spec com o código atual,
   do mesmo jeito que foi feito em RD-005/RD-007 (achar os `Row` que
   deveriam ser `FlowRow`, estados vazios genéricos demais, textos com
   "null" literal, permissões de quem vê o quê, etc. — os mesmos tipos de
   gap que apareceram nos outros specs).
4. Esperado (comum a todo o redesign, baseado no padrão dos outros specs):
   - Selos/legendas de gráfico podem vazar em 360dp/fonte 150% — se houver
     `Row` com selos de categoria/série, considerar `FlowRow` (lembrar do
     `@OptIn(ExperimentalLayoutApi::class)`, isso já causou falha de CI em
     RD-005).
   - Verificar se valores nulos/ausentes (ex.: ROI sem dado, mês sem
     projeto concluído) têm representação consistente (não literal "null",
     não zero inventado).
   - Verificar se os indicadores exibidos respeitam o perfil (liderança vê
     visão executiva; outros perfis não devem ver dados que não são deles).

## Testes

Criar o que o spec 008 pedir em "Arquivos"/"Testes e aceite" (provavelmente
um `DashboardViewModelTest.kt` com `FakeDashboardRepository`, seguindo o
padrão de `naoUsado<T>()` já usado em todos os outros `FakeXRepository`
deste projeto).

## Ao terminar

- Commit em português, push em `mobile/rd-008-dashboard` (ou continuar em
  `mobile/rd-008-dashboard-fix` se ainda não tiver PR mesclado), abrir/atualizar
  PR com rodapé `🤖 Generated with Claude Code` + link da sessão.
- Atualizar `docs/redesign/HANDOFF.md` e apagar este arquivo quando o PR
  estiver aberto.
