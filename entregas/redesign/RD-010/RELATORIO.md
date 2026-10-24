# RD-010 — Relatório de validação (parcial)

Data: 24/10/2026. Este relatório registra **o que foi de fato verificado** e **o que ainda falta** para o aceite final
do SPEC-RD-010. Não é o aceite final.

## Ambiente usado

- Emulador Android `aguia_test` (1080×2400, ~411 dp de largura) com o APK **debug** de cada branch.
- API local (`backend/`, JDK 25, `spring-boot:run`) com MongoDB 7.0 local, `APP_SEED_ENABLED=true` e
  `APP_SEED_DEMO=carregar` (contas `@aguiainova.demo`), **sem chave de IA** (cenário de leitura por regras).
- O teste de contrato (`ContratoComApiTest`, 13/13 ✅ com `AGUIA_API_URL=http://localhost:8080/`) roda no mesmo banco
  e deixa registros "Estratégia do teste …" no histórico — as capturas foram feitas depois dele, então aparecem.

## Verificado no emulador

| Spec | Verificação | Evidência |
|---|---|---|
| RD-007 | Selos quebram para a linha de baixo com fonte 150% (cartão, detalhe, histórico) | `capturas/estrategias-vigentes-fonte150.png` |
| RD-007 | Filtros categoria/campanha empilham com fonte 150% | `capturas/historico-filtro-fonte150.png` |
| RD-007 | Filtro do histórico consulta a API (só registros da categoria escolhida) | `capturas/historico-filtro-categoria.png` |
| RD-007 | Diálogo de exclusão cita o título; "Cancelar" não altera dados | `capturas/estrategia-exclusao-dialogo.png` |
| RD-008 | KPIs agrupados; pares empilham com fonte 150% | `capturas/painel-kpis-100.png`, `capturas/painel-kpis-fonte150.png` |
| RD-008 | Barra de valor zero sem preenchimento; meses legíveis | `capturas/painel-projetos-graficos.png` |
| RD-009 | Insights por regras rotulados; leitura geral explícita | `capturas/painel-insights-regras.png` |
| RD-009 | Sem provedor de IA, o chat explica o motivo em vez de botões desabilitados | `capturas/assistente-sem-ia.png` |
| RD-002 | Rótulo da barra inferior com reticências em fonte 150% (PR #17) | `capturas/barra-inferior-fonte150.png` |
| — | Login dos perfis Liderança e Gestor contra a API local | — |

## Achados durante a validação

- **Corrigido (PR #17)**: rótulo "Estratégia" da barra inferior cortado seco ("Estratég") com fonte 150%; o ícone
  repetia o rótulo para o TalkBack.
- **Corrigido (PR #16)**: chat sem IA mostrava "toque em uma pergunta" sobre botões desabilitados.
- **Aberto (RD-002)**: com fonte 150% e quatro abas (Painel), a quarta aba só aparece deslizando a fileira — não há
  pista visual de que existe.
- **Aberto (RD-003)**: no campo Senha, a ação do teclado (✓) só fecha o teclado; não envia o login.
- **Aberto (backend)**: textos de insights por regras usam ponto decimal ("13.08%"); o app usa vírgula ("13,1%").

## Não verificado (pendente para o aceite final)

- Matriz completa do SPEC-RD-010: 360 dp, IME aberta nos formulários, títulos de 120 caracteres, estados de erro e
  refresh em cada tela, sessão expirada/acesso negado.
- Fluxo integrado completo: operador cria ideia → gestor decide → cria projeto → atualiza andamento/resultado.
- Inspeção com TalkBack ligado (só a semântica foi verificada por testes Robolectric).
- Chat com IA real (balões com resposta da IA) — sem chave neste ambiente; coberto só por testes.
- APK de revisão em `entregas/redesign/` com hash SHA-256 e artefatos publicados pelo CI.
