# Arquitetura — Águia Inova (mapa de referência)

> Gerado a partir de auditoria de código completa (4 varreduras independentes: arquitetura
> backend, subsistema de IA, arquitetura mobile, prontidão de produção). Não re-auditar do
> zero — atualizar este arquivo quando a arquitetura mudar de verdade.

## Backend (`backend/`)

**Modelo arquitetural:** Arquitetura em camadas (`controller → service → repository/model`),
com o subsistema de IA em **Hexagonal / Ports & Adapters** (`service/ia` depende só de
interfaces; `integration/ai` é o adapter concreto para Gemini/OpenAI-compatível).

**Fluxo de dependência (verificado, sem vazamento):**
`Controller` → `Service` → (`Repository` Spring Data | `MongoTemplate` para consultas dinâmicas)
`Model` (domínio) ← `Mapper` ← `DTO` (request/response)
`Service` (negócio de IA) → `LlmClient`/`AvaliadorDeIdeias`/`GeradorDeInsights` (interfaces) →
`integration/ai/*` (Gemini, OpenAI-compatível, ou Null Object quando desabilitado)

**Padrões de projeto usados:**
- Strategy + Decorator — `AvaliadorComContingencia`/`GeradorInsightsComContingencia` decoram a
  estratégia de IA real com fallback automático para heurística.
- Null Object — `LlmDesabilitadoClient`.
- Repository — Spring Data MongoDB.
- DTO + Mapper — separa contrato HTTP do modelo de domínio.
- Injeção de dependência via construtor em 100% das classes (zero `@Autowired` em campo).
- Hierarquia de exceção polimórfica — `ApiException` abstrata com `status()`, subclasses
  decidem o HTTP status; `ApiExceptionHandler` não tem `instanceof`.
- Modelo de domínio sem *setters* — mutação só via método de regra
  (`Ideia.avaliar()`, `Projeto.registrarResultado()`, etc.), listas expostas como
  `Collections.unmodifiableList`.

**Clean code — avaliação:** aderente. Funções pequenas, nomes claros, SRP respeitado,
sem god classes. Desvios pontuais (não estruturais): bloco de filtro dinâmico
(`if (x != null) addCriteria(...)`) duplicado em 4 services; validação duplicada
DTO (`@NotBlank`) + domínio (`Validacoes.exigirTexto`).

## Mobile (`mobile/`)

**Modelo arquitetural:** MVVM com separação em camadas (`ui → domain → data → network`),
seguindo o padrão recomendado pelo Google para Jetpack Compose. Repository pattern com
interface de domínio (`domain/repository/Repositorios.kt`) e implementação remota
(`data/repository/*Remoto.kt`). DI manual via um único `AppContainer` (sem framework).

**Consistência:** os 6 domínios (auth, dashboard, estrategia, ia, ideia, projeto) seguem
exatamente a mesma forma — isso é o maior ponto forte do código mobile.

**Padrões de projeto usados:**
- `Resultado<T>` (sealed class Success/Failure) — evita exceção crua chegando na UI.
- Interceptor chain (OkHttp) — `AuthInterceptor` + `BaseUrlInterceptor`.
- `BaseViewModel` com `Estado` compartilhado — mantém dado anterior visível se um refresh falhar.

**Clean code — avaliação:** aderente, com gaps específicos:
- Sem camada de DTO de rede própria — Gson desserializa direto para `domain.model.*`.
- Strings de UI hardcoded em Kotlin em vez de `strings.xml` (~40 ocorrências).
- DI manual sem escopo — tudo singleton de `Application`.

## Testes

- Backend: 258 testes (unitário + integração com Testcontainers/MongoDB real), cobrindo
  inclusive caminhos de falha da IA (resposta malformada, IA fora do ar, sem chave).
  Comando: `cd backend && ./mvnw -Dmaven.compiler.release=21 test`
  (o `-Dmaven.compiler.release=21` é necessário neste ambiente de desenvolvimento, que só
  tem JDK 21 disponível; o `pom.xml` pede Java 25 — ver nota em STATUS.md).
- Mobile: testes com *fakes* (não só mocks) + 1 teste de contrato real contra API viva
  (`ContratoComApiTest`, pulado se `AGUIA_API_URL` não estiver no ambiente).
  **Limitação conhecida deste sandbox:** o Android Gradle Plugin não resolve aqui
  (sem acesso ao repositório `dl.google.com`), então testes mobile não puderam ser
  executados nesta sessão — qualquer mudança no mobile precisa ser validada em CI ou
  localmente pelo dono do projeto.

## Segurança — postura atual

**Bem feito:** JWT HS256 (exige segredo ≥32 bytes), BCrypt, RBAC via `@PreAuthorize` em
todo endpoint sensível (verificado em runtime), mensagens de erro de auth genéricas
(sem enumeração de conta), stack trace nunca exposto ao cliente.

**Furos conhecidos:** ver `STATUS.md` — specs 001, 002, 003, 007 tratam os furos de
segurança identificados.
