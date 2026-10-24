# STATUS — Águia Inova rumo a produção

Atualizado a cada ciclo do loop de TDD. Ordem = prioridade de execução.

## Autônomas (eu executo em loop TDD: RED → GREEN → REGRESSÃO)

| Spec | Título | Prioridade | Status |
|---|---|---|---|
| [001](specs/001-jwt-secret-fail-fast.md) | Falhar se JWT_SECRET for o valor-padrão | CRÍTICO | DONE |
| [002](specs/002-seed-fail-safe-default.md) | Seed fail-safe por padrão | CRÍTICO | DONE |
| [003](specs/003-rate-limiting.md) | Rate limiting login + IA | CRÍTICO | DONE (escopo reduzido) |
| [004](specs/004-dashboard-pagination.md) | Dashboard sem findAll() irrestrito | CRÍTICO | DONE (limite documentado) |
| [006](specs/006-ci-pipeline.md) | Pipeline de CI (GitHub Actions) | CRÍTICO | DONE |
| [005](specs/005-missing-indexes.md) | Índices em status/etapa/criadoEm | IMPORTANTE | DONE |
| [012](specs/012-query-builder-dedup.md) | Helper comum de filtro dinâmico | BOM TER | DONE |

## Autônomas no código, mas não verificáveis neste sandbox (sem Android SDK)

| Spec | Título | Prioridade | Status |
|---|---|---|---|
| [007](specs/007-mobile-lock-server-switch.md) | Travar troca de servidor fora de debug | CRÍTICO | DONE / não verificado |
| [008](specs/008-mobile-encrypted-session.md) | Sessão criptografada no mobile | IMPORTANTE | PENDING |

## Bloqueadas — exigem conta/credencial sua (eu não posso executar)

| Spec | Título | Prioridade | Status |
|---|---|---|---|
| [009](specs/009-mongodb-atlas-migration.md) | MongoDB Atlas (grátis) | CRÍTICO | BLOCKED |
| [010](specs/010-deploy-render-free.md) | Deploy no Render (grátis) | CRÍTICO | BLOCKED |
| [011](specs/011-mobile-distribution-no-play-store.md) | Distribuir APK sem taxa da Play Store | BOM TER | BLOCKED |

## Regra de atualização
Depois de cada spec autônoma fechar o ciclo (REGRESSÃO verde), atualizar a linha aqui para
`DONE`, atualizar o campo "Resultado" da spec correspondente com números reais de execução,
e commitar junto com o código da spec.
