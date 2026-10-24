# SPEC-001: Falhar ao iniciar se JWT_SECRET for o valor-padrão

## Status
PENDING

## Prioridade
CRÍTICO

## Execução
Autônoma (código local, backend)

## Problema
`application.yml` tem um valor-padrão para `JWT_SECRET` que é público (está no repositório).
`SecurityConfig.chaveJwt` só valida o **tamanho** (≥32 bytes) do segredo, não que ele seja
diferente do default. Um deploy em produção que esqueça de definir `JWT_SECRET` sobe
normalmente e assina tokens válidos com um segredo que qualquer um pode ler no GitHub —
qualquer pessoa pode forjar um token de Liderança.

## Critério de aceite
- [ ] A aplicação **não sobe** (lança exceção na inicialização) se `JWT_SECRET` for igual a
      qualquer um dos valores-padrão conhecidos (o do `application.yml` e o do
      `docker-compose.yml`).
- [ ] Continua subindo normalmente com qualquer segredo customizado de ≥32 bytes.
- [ ] Mensagem de erro deixa claro o motivo (não um stacktrace genérico).

## Plano TDD
1. **RED**: adicionar teste em `SecurityConfigTest` que configura `JWT_SECRET` com o valor-
   padrão literal e espera que a criação do bean `chaveJwt` lance `IllegalStateException`.
   Rodar e confirmar que FALHA hoje (porque hoje só a checagem de tamanho existe).
2. **GREEN**: em `SecurityConfig.chaveJwt`, adicionar uma constante com o(s) valor(es)-padrão
   conhecido(s) e lançar `IllegalStateException` se o segredo recebido for igual a um deles.
3. **REGRESSÃO**: `./mvnw -Dmaven.compiler.release=21 test` — suíte completa verde.

## Resultado
(preencher ao final do ciclo TDD, com números reais de execução — não antecipar)
