# SPEC-001: Falhar ao iniciar se JWT_SECRET for o valor-padrão

## Status
DONE

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
Implementado em `SecurityConfig.java`: constante `SEGREDOS_PADRAO_PROIBIDOS` (Set com os dois
valores-padrão conhecidos de `application.yml` e `docker-compose.yml`) checada em
`chaveJwt()` antes da checagem de tamanho. Teste novo:
`SecurityConfigTest.segredoPadraoConhecidoImpedeAInicializacao` (RED confirmado antes da
implementação, GREEN depois).

**Efeito colateral encontrado e corrigido durante a REGRESSÃO**: todo `@SpringBootTest` que
não definia `JWT_SECRET` explicitamente dependia do valor-padrão de desenvolvimento — agora
proibido — e passou a falhar ao subir o contexto (179 erros na primeira rodada de regressão).
Corrigido criando `backend/src/test/resources/application.yml` (sobrepõe o principal para
testes, com o mesmo segredo de teste já usado em `Dados.SEGREDO_JWT`). Spring Boot não funde
dois `application.yml` do mesmo nome em classpaths diferentes — carrega só o primeiro — então
esse arquivo precisou replicar todas as chaves do original, não só a que mudou.

Suíte completa após a correção: 259/260 verdes (a 1 falha restante é o RED intencional da
SPEC-002, ainda não implementada nesse momento).
