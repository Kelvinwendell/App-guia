# SPEC-002: Seed de dados de teste deve ser "fail-safe" por padrão

## Status
PENDING

## Prioridade
CRÍTICO

## Execução
Autônoma (código local, backend)

## Problema
`APP_SEED_ENABLED` tem valor-padrão `true` (`application.yml`, `docker-compose.yml`,
`.env.example`). Se a variável não for definida explicitamente em produção, o `DataSeeder`
cria 3 contas com senha conhecida (`123456`) — uma delas Liderança — no primeiro boot com
banco vazio. Isso é "fail-open": esquecer uma variável de ambiente abre uma brecha de
segurança sozinho.

## Critério de aceite
- [ ] Se `APP_SEED_ENABLED` não for definido, o comportamento padrão é **não semear**
      (fail-safe).
- [ ] Continua semeando normalmente quando `APP_SEED_ENABLED=true` é definido explicitamente
      (comportamento de desenvolvimento local via `docker-compose.yml`, que já define o valor
      explicitamente, continua igual).
- [ ] `docker-compose.yml` (ambiente de dev) continua com seed ligado por padrão — só o
      *default absoluto* da aplicação (sem nenhuma env var) muda para seguro.

## Plano TDD
1. **RED**: teste em `DataSeederIT` (ou novo teste) que sobe o contexto Spring **sem** definir
   `app.seed.enabled` (nem via property nem via env var) contra um banco vazio, e espera que
   NENHUM usuário seja criado. Confirmar que FALHA hoje (hoje semeia).
2. **GREEN**: mudar o default em `application.yml` de `APP_SEED_ENABLED:true` para
   `APP_SEED_ENABLED:false`; em `docker-compose.yml`, fixar explicitamente
   `APP_SEED_ENABLED: ${APP_SEED_ENABLED:-true}` → manter `true` ali mesmo (ambiente de dev é
   opt-in explícito, não depende do default da aplicação).
3. **REGRESSÃO**: `./mvnw -Dmaven.compiler.release=21 test` verde. Conferir manualmente que
   `docker compose up` (ambiente de dev) ainda semeia (smoke test já feito antes nesta sessão).

## Resultado
(preencher ao final do ciclo TDD, com números reais de execução)
