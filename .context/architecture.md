# Arquitetura — Águia Inova

## Backend (`backend/`)

**Modelo arquitetural:** arquitetura em camadas (`controller → service → repository/model`),
com o subsistema de IA em **Hexagonal / Ports & Adapters** (`service/ia` depende só de
interfaces; `integration/ai` é o adapter concreto para Gemini/OpenAI-compatível).

**Fluxo de dependência (sem vazamento entre camadas):**
```
Controller → Service → Repository (Spring Data) | MongoTemplate (consultas dinâmicas)
Model (domínio) ← Mapper ← DTO (request/response)
Service (IA) → LlmClient / AvaliadorDeIdeias / GeradorDeInsights (interfaces)
            → integration/ai/* (Gemini, OpenAI-compatível, ou Null Object quando desabilitado)
```

**Padrões de projeto usados:**
- **Strategy + Decorator** — `AvaliadorComContingencia`/`GeradorInsightsComContingencia`/
  `AnalisadorEstrategiasComContingencia`/`LeitorSaudeComContingencia`
  decoram a estratégia de IA real com *fallback* automático para heurística local quando a
  IA não está configurada ou falha. Nunca deixe essa decoração "vazar": qualquer novo recurso
  de IA deve seguir o mesmo par (implementação real + heurística + decorator de contingência).
- **Null Object** — `LlmDesabilitadoClient` em vez de `if (iaHabilitada)` espalhado pelo código.
- **Repository** — Spring Data MongoDB para consultas simples; `MongoTemplate` + os helpers de
  `util/Consultas.java` (`seIgual`, `seInformado`, `seInformadoIgnorandoCaixa`) para filtros
  dinâmicos opcionais.
- **DTO + Mapper** — separa o contrato HTTP do modelo de domínio; serviços nunca recebem DTO
  de request diretamente além da camada de mapeamento.
- **Injeção de dependência via construtor** em 100% das classes — nunca `@Autowired` em campo.
- **Hierarquia de exceção polimórfica** — `ApiException` abstrata com `status()`; cada
  subclasse decide seu HTTP status, então `ApiExceptionHandler` trata qualquer uma com um
  único `@ExceptionHandler(ApiException.class)`, sem `instanceof`. Para um erro novo, crie uma
  subclasse de `ApiException`, não um `if` novo no handler.
- **Modelo de domínio sem *setters*** — mutação só via método de regra
  (`Ideia.avaliar()`, `Projeto.registrarResultado()`, etc.), listas expostas como
  `Collections.unmodifiableList`. Novo campo mutável no domínio precisa de um método com
  nome de verbo/regra de negócio, nunca um `setX`.
- **Rate limiting em memória** — `security/LimitadorDeTaxa` (janela fixa por chave,
  `ConcurrentHashMap`), sem dependência externa nem serviço como Redis — adequado porque a
  API roda numa instância só. Se o projeto algum dia escalar para múltiplas instâncias, isso
  precisa virar um serviço compartilhado (Redis, por exemplo).

## Mobile (`mobile/`)

**Modelo arquitetural:** MVVM com separação em camadas (`ui → domain → data → network`),
padrão recomendado pelo Google para Jetpack Compose. Repository pattern com interface de
domínio (`domain/repository/Repositorios.kt`) e implementação remota
(`data/repository/*Remoto.kt`). DI manual via um único `AppContainer` (sem framework como
Hilt — avaliado e considerado suficiente para o tamanho atual do projeto).

**Consistência:** os 6 domínios (auth, dashboard, estrategia, ia, ideia, projeto) seguem
exatamente a mesma forma — ao adicionar um sétimo domínio, replique essa forma em vez de
inventar uma nova.

**Padrões de projeto usados:**
- `Resultado<T>` (sealed class Success/Failure) — evita exceção crua chegando na UI; toda
  chamada de rede deve passar por esse tipo.
- Interceptor chain (OkHttp) — `AuthInterceptor` (injeta token, detecta 401) +
  `BaseUrlInterceptor` (permite trocar servidor em runtime, só em build debug).
- `BaseViewModel` com `Estado` compartilhado — mantém dado anterior visível se um refresh
  falhar, em vez de apagar a tela. Todo ViewModel novo deve herdar desse padrão.

**Gaps conhecidos, aceitos conscientemente por ora:**
- Sem camada de DTO de rede própria — Gson desserializa direto para `domain.model.*`. Uma
  mudança de campo no backend quebra o app sem tradução no meio. Resolver quando o contrato
  da API começar a mudar com frequência.
- Strings de UI hardcoded em Kotlin em vez de `strings.xml`. Resolver se/quando o projeto
  precisar de internacionalização.

## Testes

- **Backend:** suíte de unidade + integração com Testcontainers (MongoDB real e descartável
  a cada rodada, não mock). Comando: `cd backend && ./mvnw test`. Cobre inclusive os caminhos
  de falha da IA (resposta malformada, IA fora do ar, sem chave configurada) — qualquer novo
  comportamento de IA deve ter o equivalente testado, não só o caminho feliz.
- **Mobile:** testes com *fakes* escritos à mão (não mocks genéricos) para dependências com
  `Context` do Android; para instanciar uma classe que depende de `Context` em teste JVM puro,
  use **Robolectric** (`@RunWith(RobolectricTestRunner::class)` +
  `ApplicationProvider.getApplicationContext()`), como em `CapturasDeTelaTest` e
  `ConfiguracaoServidorEditavelTest`. Há também um teste de contrato real contra API viva
  (`ContratoComApiTest`, pulado automaticamente se `AGUIA_API_URL` não estiver definida).
- **CI:** GitHub Actions (`.github/workflows/backend-ci.yml`) roda a suíte do backend em todo
  push/PR. Não há job de mobile ainda — precisa de um runner com Android SDK configurado
  (o GitHub Actions oferece isso nativamente em `ubuntu-latest`/`macos-latest`, via
  `android-actions/setup-android`); ao adicionar, siga o mesmo padrão do job de backend.

## Segurança — postura atual

JWT HS256 com segredo obrigatório de ≥32 bytes **e diferente de qualquer valor-padrão
conhecido do repositório** (a API recusa subir caso contrário), BCrypt para senha, RBAC via
`@PreAuthorize` em todo endpoint sensível, mensagens de erro de autenticação genéricas (sem
enumeração de conta), stack trace nunca exposto ao cliente, rate limiting em login e
endpoints de IA, sessão do app mobile criptografada e troca de servidor só em build de debug.
Detalhe completo de cada regra e o porquê em [`security-invariants.md`](security-invariants.md).
